package com.lab.emp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求体：只接收员工姓名与口令，员工编号由服务端查询补全。
 */
public class LoginForm {

    @NotBlank(message = "登录账号不能为空")
    @Size(max = 50, message = "登录账号长度不能超过 50 个字符")
    private String empName;

    @NotBlank(message = "登录口令不能为空")
    @Size(min = 6, max = 32, message = "口令长度需在 6 到 32 个字符之间")
    private String password;

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
