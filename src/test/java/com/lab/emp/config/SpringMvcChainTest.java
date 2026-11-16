package com.lab.emp.config;

import com.lab.emp.probe.ProbeWebConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SpringMVC 链路验证：不启动 Tomcat，用 MockMvc 模拟一次请求，
 * 验证 DispatcherServlet、组件扫描、消息转换器与统一返回格式。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ProbeWebConfig.class})
public class SpringMvcChainTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void requestChainWorks() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(get("/probe/hello"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":200")));
    }
}
