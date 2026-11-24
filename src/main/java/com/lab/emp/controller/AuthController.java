package com.lab.emp.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lab.emp.common.Result;
import com.lab.emp.dto.LoginForm;
import com.lab.emp.entity.Emp;
import com.lab.emp.interceptor.LoginInterceptor;
import com.lab.emp.service.EmpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证接口：实验环境不接统一用户中心，用 emp 表 + 统一演示口令完成登录。
 * <p>
 * 登录成功后把员工编号与姓名写入 Session，由 LoginInterceptor 校验后续请求。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /** 演示口令：仅用于课程实验，正式环境应改为账号体系 + 加密存储 */
    private static final String DEMO_PASSWORD = "emp@2026";

    private final EmpService empService;

    @Autowired
    public AuthController(EmpService empService) {
        this.empService = empService;
    }

    /** 登录：账号为员工姓名，且在岗状态必须为 1 */
    @PostMapping("/login")
    public Result<Void> login(@RequestBody @Valid LoginForm form, HttpSession session) {
        Emp emp = empService.getOne(Wrappers.<Emp>lambdaQuery()
                .eq(Emp::getEmpName, form.getEmpName().trim())
                .eq(Emp::getStatus, 1)
                .last("LIMIT 1"));
        if (emp == null) {
            return Result.fail(401, "账号不存在或员工已离岗");
        }
        if (!DEMO_PASSWORD.equals(form.getPassword())) {
            return Result.fail(401, "口令不正确");
        }
        session.setAttribute(LoginInterceptor.SESSION_EMP_ID, emp.getEmpId());
        session.setAttribute(LoginInterceptor.SESSION_EMP_NAME, emp.getEmpName());
        log.info("员工 {}（empId={}）登录成功", emp.getEmpName(), emp.getEmpId());
        return Result.ok();
    }

    /** 退出登录：销毁当前会话 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        String empName = (String) session.getAttribute(LoginInterceptor.SESSION_EMP_NAME);
        session.invalidate();
        log.info("员工 {} 已退出登录", empName);
        return Result.ok();
    }

    /** 当前登录信息：前端刷新页面时用来恢复登录态 */
    @GetMapping("/me")
    public Result<Map<String, Object>> me(HttpSession session) {
        Object empId = session.getAttribute(LoginInterceptor.SESSION_EMP_ID);
        if (empId == null) {
            return Result.unauthorized();
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("empId", empId);
        data.put("empName", session.getAttribute(LoginInterceptor.SESSION_EMP_NAME));
        return Result.ok(data);
    }
}
