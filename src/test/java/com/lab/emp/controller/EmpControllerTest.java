package com.lab.emp.controller;

import com.lab.emp.config.MyBatisPlusConfig;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.SpringMvcConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    /** 分页接口：返回 code=200 且包含 MP 分页字段 total/pages */
    @Test
    public void pageApiWorks() throws Exception {
        mockMvc.perform(get("/emps").param("page", "1").param("size", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")))
                .andExpect(content().string(containsString("\"total\"")))
                .andExpect(content().string(containsString("\"pages\"")));
    }

    /** 单查接口：返回员工姓名 */
    @Test
    public void getByIdApiWorks() throws Exception {
        mockMvc.perform(get("/emps/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")))
                .andExpect(content().string(containsString("empName")));
    }

    /** 分页边界：page=0 应被拦截并返回 400 */
    @Test
    public void invalidPageParamRejected() throws Exception {
        mockMvc.perform(get("/emps").param("page", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")));
    }

    /** 多条件：不存在的姓名应返回 total=0，证明模糊条件已进入 SQL */
    @Test
    public void multiConditionFiltersWorks() throws Exception {
        mockMvc.perform(get("/emps")
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
        mockMvc.perform(get("/emps")
                        .param("page", "1")
                        .param("size", "5")
                        .param("minSalary", "8000")
                        .param("maxSalary", "20000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")))
                .andExpect(content().string(containsString("\"records\"")));
    }

    /** 批量删除空数组应返回 400，不应该走 SQL */
    @Test
    public void emptyBatchDeleteRejected() throws Exception {
        mockMvc.perform(delete("/emps").contentType("application/json").content("[]"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":400")));
    }
}
