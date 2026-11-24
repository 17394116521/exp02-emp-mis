package com.lab.emp.service.impl;

import com.lab.emp.common.BusinessException;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.EmpTxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 事务验证实现：迁移逻辑本身是一致的，差别只在事务边界是否真正生效。
 */
@Service
public class EmpTxServiceImpl implements EmpTxService {

    private static final Logger log = LoggerFactory.getLogger(EmpTxServiceImpl.class);

    /** 故意使用的非法员工编号，用来在更新若干行后触发失败 */
    public static final Integer MISSING_EMP_ID = 999999;

    private final EmpMapper empMapper;

    @Autowired
    public EmpTxServiceImpl(EmpMapper empMapper) {
        this.empMapper = empMapper;
    }

    /** 正常事务：整批迁移，失败整体回滚 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int migrateDept(List<Integer> empIds, String deptName, Integer deptId) {
        log.info("开始迁移部门，目标 {}，员工 {}", deptName, empIds);
        return doMigrate(empIds, deptName, deptId);
    }

    /** 失效场景一：外层事务方法内部把异常吃掉，事务认为一切正常，最终提交 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int migrateDeptSwallowingException(List<Integer> empIds, String deptName, Integer deptId) {
        try {
            return doMigrate(empIds, deptName, deptId);
        } catch (BusinessException e) {
            // 异常在此终止，没有继续向上抛，事务管理器不会标记回滚
            log.warn("异常被捕获并吞掉：{}，事务不会回滚", e.getMessage());
            return -1;
        }
    }

    /** 失效场景二：this 调用同类方法，不经过代理，@Transactional 被忽略 */
    @Override
    public int migrateDeptBySelfInvocation(List<Integer> empIds, String deptName, Integer deptId) {
        return this.migrateDept(empIds, deptName, deptId);
    }

    /** 失效场景三：@Transactional 标注在 private 方法上，代理无法织入 */
    @Override
    public int migrateDeptViaPrivateMethod(List<Integer> empIds, String deptName, Integer deptId) {
        return applyWithPrivateAnnotation(empIds, deptName, deptId);
    }

    /**
     * 逐条迁移：先查存在性再更新，任何一条缺失立即抛异常。
     * 正因为是「先改了几条后失败」，才能看出回滚是否生效。
     */
    private int doMigrate(List<Integer> empIds, String deptName, Integer deptId) {
        if (empIds == null || empIds.isEmpty()) {
            throw new BusinessException(400, "迁移员工列表不能为空");
        }
        int affected = 0;
        for (Integer empId : empIds) {
            Emp emp = empMapper.selectById(empId);
            if (emp == null) {
                throw new BusinessException(404, "员工不存在，empId=" + empId);
            }
            Emp patch = new Emp();
            patch.setEmpId(empId);
            patch.setDept(deptName);
            patch.setDeptId(deptId);
            affected += empMapper.updateById(patch);
        }
        return affected;
    }

    /** private 上的 @Transactional 只是普通注解，不会被事务基础设施识别 */
    @Transactional(rollbackFor = Exception.class)
    private int applyWithPrivateAnnotation(List<Integer> empIds, String deptName, Integer deptId) {
        return doMigrate(empIds, deptName, deptId);
    }
}
