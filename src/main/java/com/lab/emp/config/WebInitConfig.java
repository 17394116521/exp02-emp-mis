package com.lab.emp.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;

/**
 * Web 容器初始化：替代 web.xml，注册 DispatcherServlet 与字符编码过滤器。
 * Tomcat 9 启动时通过 SPI 自动发现本类。
 */
public class WebInitConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    /** Spring 根容器：Service / Mapper / 事务 / 数据源 */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{SpringConfig.class, MyBatisPlusConfig.class};
    }

    /** SpringMVC 子容器：Controller */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{SpringMvcConfig.class};
    }

    /** DispatcherServlet 拦截所有请求 */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /** 统一 UTF-8 编码，解决 POST 请求中文乱码 */
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);
        return new Filter[]{encodingFilter};
    }
}
