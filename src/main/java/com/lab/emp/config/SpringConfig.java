package com.lab.emp.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Spring 根容器配置：数据源 + MyBatis-Plus 会话工厂 + Mapper 扫描 + 声明式事务。
 * <p>
 * 说明：SSM 环境没有 Spring Boot 自动配置，MP 的 SqlSessionFactory 必须使用
 * {@link MybatisSqlSessionFactoryBean} 手工装配，否则 BaseMapper 中的通用方法
 * 无法注入，运行时会抛出 Invalid bound statement (not found)。
 * 分页拦截器同样需要显式 setPlugins，仅声明成 Bean 不会自动生效。
 */
@Configuration
@ComponentScan({"com.lab.emp.service", "com.lab.emp.common"})
@PropertySource("classpath:jdbc.properties")
@MapperScan("com.lab.emp.mapper")
@EnableTransactionManagement
public class SpringConfig {

    /** Druid 连接池，连接四要素来自 jdbc.properties，避免硬编码 */
    @Bean
    public DataSource dataSource(Environment env) {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName(env.getProperty("jdbc.driver"));
        ds.setUrl(env.getProperty("jdbc.url"));
        ds.setUsername(env.getProperty("jdbc.username"));
        ds.setPassword(env.getProperty("jdbc.password"));
        // 初始连接数 5，最大活跃 20，与实验指导书建议一致
        ds.setInitialSize(5);
        ds.setMaxActive(20);
        ds.setValidationQuery("SELECT 1");
        return ds;
    }

    /** MyBatis-Plus 会话工厂：驼峰映射、实体别名、分页插件 */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
                                               MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTypeAliasesPackage("com.lab.emp.entity");
        // 注册 MP 插件（分页）
        factoryBean.setPlugins(mybatisPlusInterceptor);

        // 必须使用 MP 的 MybatisConfiguration，才能接管 BaseMapper 的 SQL 注入
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        // SQL 日志交给 SLF4J，由 logback 输出到控制台
        configuration.setLogImpl(Slf4jImpl.class);
        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }

    /** 声明式事务管理器，配合 @EnableTransactionManagement 与 @Transactional 使用 */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
