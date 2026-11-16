package com.lab.emp.probe;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 仅用于 MockMvc 链路验证的 SpringMVC 测试配置。
 * 只扫描探针包，避免把依赖根容器业务 Bean 的正式 Controller 拉进来。
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.lab.emp.probe")
public class ProbeWebConfig {
}
