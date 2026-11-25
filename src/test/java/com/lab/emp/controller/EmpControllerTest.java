package com.lab.emp.controller;

import com.lab.emp.config.MyBatisPlusConfig;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.SpringMvcConfig;
import com.lab.emp.interceptor.LoginInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 员工接口测试：验证 RESTful 路径、统一返回格式与分页插件是否生效。
 * 只做查询类请求，不向业务库写入数据。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringConfig.class, MyBatisPlusConfig.class, SpringMvcConfig.class})
public class EmpControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    /** 构造一个已登录会话，模拟 LoginInterceptor 放行条件 */
    private MockHttpSession loginSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LoginInterceptor.SESSION_EMP_ID, 1);
        session.setAttribute(LoginInterceptor.SESSION_EMP_NAME, "顾清晏");
        return session;
    }

    /** 分页接口：返回 code=200 且包含 MP 分页字段 total/pages */
    @Test
    public void pageApiWorks() throws Exception {
        mockMvc.perform(get("/emps").session(loginSession()).param("page", "1").param("size", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")))
                .andExpect(content().string(containsString("\"total\"")))
                .andExpect(content().string(containsString("\"pages\"")));
    }

    /** 单查接口：返回员工姓名 */
    @Test
    public void getByIdApiWorks() throws Exception {
        mockMvc.perform(get("/emps/1").session(loginSession()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")))
                .andExpect(content().string(containsString("empName")));
    }

    /** 分页边界：page=0 应被拦截并返回 400 */
    @Test
    public void invalidPageParamRejected() throws Exception {
        mockMvc.perform(get("/emps").session(loginSession()).param("page", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")));
    }

    /** 多条件：不存在的姓名应返回 total=0，证明模糊条件已进入 SQL */
    @Test
    public void multiConditionFiltersWorks() throws Exception {
        mockMvc.perform(get("/emps").session(loginSession())
                        .param("page", "1")
                        .param("size", "5")
                        .param("name", "该员工不存在XYZ")
                        .param("gender", "男")
                        .param("dept", "技术部"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")))
                .andExpect(content().string(containsString("\"total\":0")));
    }

    /** 薪资区间：只传下限也应正常返回分页结构 */
    @Test
    public void salaryRangeConditionWorks() throws Exception {
        mockMvc.perform(get("/emps").session(loginSession())
                        .param("page", "1")
                        .param("size", "5")
                        .param("minSalary", "8000")
                        .param("maxSalary", "20000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")))
                .andExpect(content().string(containsString("\"records\"")));
    }

    /** 参数校验：姓名为空白应被拦截并返回 400 */
    @Test
    public void blankNameRejected() throws Exception {
        mockMvc.perform(post("/emps").session(loginSession()).contentType("application/json")
                        .content("{\"empName\":\"   \",\"salary\":10000}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("员工姓名不能为空")));
    }

    /** 参数校验：负薪资应被拦截并返回 400 */
    @Test
    public void negativeSalaryRejected() throws Exception {
        mockMvc.perform(post("/emps").session(loginSession()).contentType("application/json")
                        .content("{\"empName\":\"测试员工\",\"salary\":-1}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("薪资不能为负数")));
    }

    /** 参数校验：性别不在允许值内应被拦截 */
    @Test
    public void illegalGenderRejected() throws Exception {
        mockMvc.perform(post("/emps").session(loginSession()).contentType("application/json")
                        .content("{\"empName\":\"校验性别\",\"gender\":\"未知\",\"salary\":9000}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("性别只能填写男或女")));
    }

    /** 全局异常：非法 JSON 应返回统一的 400 结构，而不是容器错误页 */
    @Test
    public void malformedJsonRejected() throws Exception {
        mockMvc.perform(post("/emps").session(loginSession()).contentType("application/json")
                        .content("{empName:}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("请求体格式错误")));
    }

    /** 批量删除空数组应返回 400，不应该走 SQL */
    @Test
    public void emptyBatchDeleteRejected() throws Exception {
        mockMvc.perform(delete("/emps").session(loginSession()).contentType("application/json").content("[]"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")));
    }

    /** 复查补充：修改接口不带 empId 时应返回 400，避免 updateById 静默失败被当成成功 */
    @Test
    public void updateWithoutIdRejected() throws Exception {
        mockMvc.perform(put("/emps").session(loginSession()).contentType("application/json")
                        .content("{\"empName\":\"无主键更新\",\"salary\":9000}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("必须提供合法的 empId")));
    }

    /** 复查补充：薪资下限大于上限属于无效区间，应直接拦截，不再发出必然空结果的 SQL */
    @Test
    public void reversedSalaryRangeRejected() throws Exception {
        mockMvc.perform(get("/emps").session(loginSession())
                        .param("minSalary", "20000")
                        .param("maxSalary", "8000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("薪资区间不合法")));
    }

    /** 复查补充：page 传非数字应归为参数错误，而不是落到兜底分支报 500 */
    @Test
    public void nonNumericPageRejected() throws Exception {
        mockMvc.perform(get("/emps").session(loginSession()).param("page", "abc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("请求参数[page]")));
    }

    /** 复查补充：主键为 0 或负数应返回 400，不查询数据库 */
    @Test
    public void illegalIdRejected() throws Exception {
        mockMvc.perform(get("/emps/0").session(loginSession()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")))
                .andExpect(content().string(containsString("员工编号必须为正整数")));
    }

    /** AOP：请求日志切面应被容器注册，Controller 调用会被代理 */
    @Test
    public void traceAspectRegistered() {
        org.junit.Assert.assertTrue("请求日志切面应注册为 Bean",
                webApplicationContext.containsBean("requestTraceAspect"));
    }

    /** 登录拦截：不带会话访问员工列表应被拦截，返回 401 与统一结构 */
    @Test
    public void requestWithoutSessionBlocked() throws Exception {
        mockMvc.perform(get("/emps"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":401")))
                .andExpect(content().string(containsString("未登录或登录已过期")));
    }
}
