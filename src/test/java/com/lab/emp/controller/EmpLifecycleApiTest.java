package com.lab.emp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.emp.config.MyBatisPlusConfig;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.SpringMvcConfig;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.EmpService;
import org.junit.After;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 任务 7 前半：登录 → 新增 → 单查 → 修改 → 条件列表 → 删除 的完整接口链路验证。
 * 用例结束按员工编号清理自己创建的临时数据，保证库内不残留测试记录。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringConfig.class, MyBatisPlusConfig.class, SpringMvcConfig.class})
public class EmpLifecycleApiTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmpService empService;

    private MockMvc mockMvc;

    private MockHttpSession session;

    /** 链路中创建的临时员工编号，用于收尾清理 */
    private Integer createdId;

    private String uniqueName;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
        uniqueName = "链路验证-" + (System.nanoTime() % 1000000);

        session = new MockHttpSession();
        String onJobName = empService.getOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.<Emp>lambdaQuery()
                .eq(Emp::getStatus, 1)
                .orderByAsc(Emp::getEmpId)
                .last("LIMIT 1")).getEmpName();
        mockMvc.perform(post("/auth/login").session(session)
                        .contentType("application/json")
                        .content("{\"empName\":\"" + onJobName + "\",\"password\":\"emp@2026\"}"))
                .andExpect(content().string(containsString("\"code\":200")));
    }

    @After
    public void cleanUp() {
        if (createdId != null) {
            empService.removeById(createdId);
        }
    }

    @Test
    public void fullCrudLifecycle() throws Exception {
        // 1. 新增
        mockMvc.perform(post("/emps").session(session)
                        .contentType("application/json")
                        .content("{\"empName\":\"" + uniqueName + "\",\"gender\":\"女\","
                                + "\"dept\":\"数智研发中心\",\"post\":\"链路验证岗\","
                                + "\"salary\":8800,\"status\":1,\"deptId\":2}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")));

        // 2. 按姓名条件列表查回编号，验证写入与模糊查询
        MvcResult listResult = mockMvc.perform(get("/emps").session(session)
                        .param("page", "1").param("size", "5").param("name", uniqueName))
                .andExpect(content().string(containsString("\"code\":200")))
                .andReturn();
        JsonNode root = JSON.readTree(listResult.getResponse().getContentAsString());
        createdId = root.path("data").path("records").get(0).path("empId").asInt();
        org.junit.Assert.assertTrue("应能在列表中查到刚新增的员工", createdId > 0);

        // 3. 单查
        mockMvc.perform(get("/emps/" + createdId).session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(uniqueName)));

        // 4. 修改薪资
        mockMvc.perform(put("/emps").session(session)
                        .contentType("application/json")
                        .content("{\"empId\":" + createdId + ",\"empName\":\"" + uniqueName + "\",\"salary\":9200}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")));

        // 5. 确认修改生效
        mockMvc.perform(get("/emps/" + createdId).session(session))
                .andExpect(content().string(containsString("9200")));

        // 6. 删除并确认记录不存在
        mockMvc.perform(delete("/emps/" + createdId).session(session))
                .andDo(print())
                .andExpect(content().string(containsString("\"code\":200")));
        createdId = null;

        mockMvc.perform(get("/emps/" + root.path("data").path("records").get(0).path("empId").asInt())
                        .session(session))
                .andExpect(content().string(containsString("\"data\":null")));
    }
}
