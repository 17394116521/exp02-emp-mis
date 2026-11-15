package com.lab.emp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置：分页拦截器。
 * <p>
 * 注意：非 Spring Boot 环境下，该拦截器必须在 SpringConfig 的
 * SqlSessionFactory 中通过 setPlugins 注册才会生效，只声明 Bean 不生效。
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定数据库类型，分页方言才会按 MySQL 的 LIMIT 生成
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(100L);
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
