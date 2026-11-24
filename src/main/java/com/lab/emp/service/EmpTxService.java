package com.lab.emp.service;

import java.util.List;

/**
 * 声明式事务验证服务（实验二任务 6.5）。
 * <p>
 * 用「部门批量迁移」作为事务载体：一次调用会更新多行员工记录，
 * 若中途失败，已改动的记录必须整体回滚；同时提供三种典型失效场景的方法，
 * 用于对比现象、记录排查结论。
 */
public interface EmpTxService {

    /**
     * 正常事务方法：任一员工不存在即抛出业务异常，之前已更新的记录全部回滚。
     *
     * @return 实际更新的行数
     */
    int migrateDept(List<Integer> empIds, String deptName, Integer deptId);

    /**
     * 失效场景一：异常被 try-catch 吞掉，事务感知不到失败，最终提交。
     *
     * @return 正常返回更新行数，异常被吞时返回 -1
     */
    int migrateDeptSwallowingException(List<Integer> empIds, String deptName, Integer deptId);

    /**
     * 失效场景二：同类方法用 this 自调用，绕过 Spring 代理，@Transactional 不生效。
     */
    int migrateDeptBySelfInvocation(List<Integer> empIds, String deptName, Integer deptId);

    /**
     * 失效场景三：注解加在 private 方法上，代理无法织入，注解被忽略。
     */
    int migrateDeptViaPrivateMethod(List<Integer> empIds, String deptName, Integer deptId);
}
