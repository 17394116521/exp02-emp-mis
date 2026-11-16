package com.lab.emp.config;

import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.EmpService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 整合验证测试：验证 Spring 根容器、数据源、MyBatis-Plus 会话工厂、
 * Mapper 代理与事务管理器的装配是否可用。
 * <p>
 * 该测试替代指导书中“临时 Service 方法验证数据库连通”的做法，
 * 可重复执行，且不会向业务表写入脏数据。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class, MyBatisPlusConfig.class})
public class SsmIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EmpMapper empMapper;

    @Autowired
    private EmpService empService;

    /** 四个核心 Bean 均成功注入，说明根容器装配无遗漏 */
    @Test
    public void contextBeansReady() {
        assertNotNull("数据源未注入", dataSource);
        assertNotNull("SqlSessionFactory 未注入", sqlSessionFactory);
        assertNotNull("事务管理器未注入", transactionManager);
        assertNotNull("Mapper 代理未注入", empMapper);
        assertNotNull("Service 代理未注入", empService);
        System.out.println("[整合验证] 数据源实现类 = " + dataSource.getClass().getName());
        System.out.println("[整合验证] 事务管理器实现类 = " + transactionManager.getClass().getName());
    }

    /** 真实获取一次数据库连接，验证连接四要素与 Druid 配置 */
    @Test
    public void dataSourceConnected() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM emp")) {
            assertTrue("数据库连接不可用", conn.isValid(2));
            assertTrue(rs.next());
            int total = rs.getInt(1);
            System.out.println("[整合验证] emp 表当前记录数 = " + total);
            assertTrue("emp 表没有数据，请先执行 sql/init.sql", total > 0);
        }
    }

    /** 通过 MP 的 BaseMapper 查询，验证 SqlSessionFactory 与实体映射 */
    @Test
    public void mapperQueryWorks() {
        List<Emp> list = empMapper.selectList(null);
        assertFalse("分页/查询结果为空", list.isEmpty());
        Emp first = list.get(0);
        assertNotNull("主键映射失败", first.getEmpId());
        assertNotNull("emp_name 未映射到 empName", first.getEmpName());
        System.out.println("[整合验证] 查询到 " + list.size() + " 条员工记录，第一条 = " + first.getEmpName());
    }

    /** Service 层走 MP 的 IService，验证事务代理与 Service 扫描 */
    @Test
    public void serviceProxyWorks() {
        Emp emp = empService.getById(1);
        assertNotNull("Service 查询失败", emp);
        assertEquals("员工编号映射错误", Integer.valueOf(1), emp.getEmpId());
        System.out.println("[整合验证] Service 查询结果 = " + emp);
    }
}
