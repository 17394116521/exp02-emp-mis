package com.lab.emp.service;

import com.lab.emp.common.BusinessException;
import com.lab.emp.config.MyBatisPlusConfig;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.impl.EmpTxServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

/**
 * 声明式事务验证（任务 6.5）：
 * 用两条临时员工数据验证「正常回滚」与三种典型事务失效场景，
 * 用例结束统一删除临时数据，不污染业务库。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class, MyBatisPlusConfig.class})
public class EmpTxServiceTest {

    private static final String NAME_PREFIX = "事务验证-";
    private static final String OLD_DEPT = "原部门-验证";
    private static final String NEW_DEPT = "迁移部门-验证";

    @Autowired
    private EmpTxService empTxService;

    @Autowired
    private EmpMapper empMapper;

    private Emp empA;
    private Emp empB;

    @Before
    public void prepareData() {
        String suffix = String.valueOf(System.nanoTime() % 100000);
        empA = insertTemp("事务验证甲-" + suffix);
        empB = insertTemp("事务验证乙-" + suffix);
    }

    /** 用例结束删除临时数据，保证库中只留下真实业务数据 */
    @After
    public void cleanData() {
        if (empA != null && empA.getEmpId() != null) {
            empMapper.deleteById(empA.getEmpId());
        }
        if (empB != null && empB.getEmpId() != null) {
            empMapper.deleteById(empB.getEmpId());
        }
    }

    /** 正常场景：全部员工存在时整批迁移成功 */
    @Test
    public void migrateAllSuccessfully() {
        int rows = empTxService.migrateDept(Arrays.asList(empA.getEmpId(), empB.getEmpId()), NEW_DEPT, 2);
        Assert.assertEquals(2, rows);
        Assert.assertEquals(NEW_DEPT, empMapper.selectById(empA.getEmpId()).getDept());
        Assert.assertEquals(NEW_DEPT, empMapper.selectById(empB.getEmpId()).getDept());
    }

    /** 事务生效：中途遇到不存在的编号抛异常，已更新的记录回滚 */
    @Test
    public void rollbackWhenAnyEmpMissing() {
        try {
            empTxService.migrateDept(Arrays.asList(empA.getEmpId(), EmpTxServiceImpl.MISSING_EMP_ID), NEW_DEPT, 2);
            Assert.fail("应抛出 BusinessException");
        } catch (BusinessException e) {
            System.out.println("[回滚验证] 捕获异常：" + e.getMessage());
        }
        Assert.assertEquals("事务应回滚，部门保持原值", OLD_DEPT,
                empMapper.selectById(empA.getEmpId()).getDept());
    }

    /** 失效场景一：异常被吞，事务照常提交，数据未回滚 */
    @Test
    public void swallowedExceptionCommits() {
        int result = empTxService.migrateDeptSwallowingException(
                Arrays.asList(empA.getEmpId(), EmpTxServiceImpl.MISSING_EMP_ID), NEW_DEPT, 2);
        Assert.assertEquals(-1, result);
        Assert.assertEquals("异常被吞后事务已提交，部门被改", NEW_DEPT,
                empMapper.selectById(empA.getEmpId()).getDept());
    }

    /** 失效场景二：this 自调用绕过代理，事务不生效，抛出异常也不回滚 */
    @Test
    public void selfInvocationLosesTransaction() {
        try {
            empTxService.migrateDeptBySelfInvocation(
                    Arrays.asList(empA.getEmpId(), EmpTxServiceImpl.MISSING_EMP_ID), NEW_DEPT, 2);
            Assert.fail("应抛出 BusinessException");
        } catch (BusinessException e) {
            System.out.println("[自调用验证] 捕获异常：" + e.getMessage());
        }
        Assert.assertEquals("自调用无事务，已更新记录不会回滚", NEW_DEPT,
                empMapper.selectById(empA.getEmpId()).getDept());
    }

    /** 失效场景三：@Transactional 标在 private 方法上被忽略，同样不回滚 */
    @Test
    public void privateMethodAnnotationIgnored() {
        try {
            empTxService.migrateDeptViaPrivateMethod(
                    Arrays.asList(empA.getEmpId(), EmpTxServiceImpl.MISSING_EMP_ID), NEW_DEPT, 2);
            Assert.fail("应抛出 BusinessException");
        } catch (BusinessException e) {
            System.out.println("[private 注解验证] 捕获异常：" + e.getMessage());
        }
        Assert.assertEquals("private 方法上的注解不生效，数据不回滚", NEW_DEPT,
                empMapper.selectById(empA.getEmpId()).getDept());
    }

    private Emp insertTemp(String empName) {
        Emp emp = new Emp();
        emp.setEmpName(empName);
        emp.setGender("女");
        emp.setDept(OLD_DEPT);
        emp.setPost("事务验证岗");
        emp.setSalary(new BigDecimal("9000.00"));
        emp.setHireDate(new Date());
        emp.setStatus(1);
        emp.setDeptId(1);
        empMapper.insert(emp);
        return emp;
    }
}
