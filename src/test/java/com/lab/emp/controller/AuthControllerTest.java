package com.lab.emp.controller;

import com.lab.emp.config.MyBatisPlusConfig;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.SpringMvcConfig;
import com.lab.emp.entity.Emp;
import com.lab.emp.interceptor.LoginInterceptor;
import com.lab.emp.service.EmpService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证与登录拦截测试：登录成功后同一会话可访问受保护接口，
 * 退出后再次访问应被拦截。只发只读请求，不写库。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringConfig.class, MyBatisPlusConfig.class, SpringMvcConfig.class})
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmpService empService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    /** 取一个真实在岗员工，避免测试写死姓名导致与数据库不一致 */
    private String onJobEmpName() {
        Emp emp = empService.getOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.<Emp>lambdaQuery()
                .eq(Emp::getStatus, 1)
                .orderByAsc(Emp::getEmpId)
                .last("LIMIT 1"));
        return emp == null ? "" : emp.getEmpName();
    }

    /** 登录成功：返回 200，Session 写入员工编号，随后可访问 /emps */
    @Test
    public void loginThenAccessProtectedApi() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/auth/login").session(session)
                        .contentType("application/json")
                        .content("{\"empName\":\"" + onJobEmpName() + "\",\"password\":\"emp@2026\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")));

        Object empId = session.getAttribute(LoginInterceptor.SESSION_EMP_ID);
        org.junit.Assert.assertNotNull("登录后会话应写入员工编号", empId);

        mockMvc.perform(get("/emps").session(session).param("page", "1").param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")));
    }

    /** 口令错误：返回 401 且不建立登录态 */
    @Test
    public void loginWithWrongPasswordRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/auth/login").session(session)
                        .contentType("application/json")
                        .content("{\"empName\":\"" + onJobEmpName() + "\",\"password\":\"wrong-pass\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":401")))
                .andExpect(content().string(containsString("口令不正确")));

        org.junit.Assert.assertNull("失败登录不应写入会话", session.getAttribute(LoginInterceptor.SESSION_EMP_ID));
    }

    /** 退出登录：销毁会话后再访问受保护接口应返回 401 */
    @Test
    public void logoutThenBlocked() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/auth/login").session(session)
                        .contentType("application/json")
                        .content("{\"empName\":\"" + onJobEmpName() + "\",\"password\":\"emp@2026\"}"))
                .andDo(print())
                .andExpect(content().string(containsString("\"code\":200")));

        mockMvc.perform(post("/auth/logout").session(session))
                .andDo(print())
                .andExpect(content().string(containsString("\"code\":200")));

        org.junit.Assert.assertTrue("退出后会话应被销毁", session.isInvalid());

        // 会话已失效，前端后续请求只带空会话，应被拦截器拦下
        mockMvc.perform(get("/emps").session(new MockHttpSession()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":401")));
    }

    /** 未登录查看 /auth/me：返回 401 统一结构 */
    @Test
    public void meWithoutLoginReturns401() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/me").session(new MockHttpSession()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        org.junit.Assert.assertTrue(result.getResponse().getContentAsString().contains("\"code\":401"));
    }
}
