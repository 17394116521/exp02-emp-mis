# 实验二：AI 赋能 SSM 框架整合与 EMP 员工管理 MIS 系统开发
## 📌 实验信息
- 环境：IDEA、Maven 3.9.6、JDK 11、MySQL 8.0、Tomcat 9、Spring 5.3.31、SpringMVC 5.3.31、MyBatis-Plus 3.5.3.1、Druid 1.2.20
- 版本管理：Git，分阶段提交保留开发时间线

## 📋 实验目的
1. 独立完成 SSM 全注解框架整合，掌握根容器与 SpringMVC 子容器的职责划分
2. 掌握 Druid 数据源、MyBatis-Plus 会话工厂、Mapper 扫描与声明式事务的装配
3. 按 Controller–Service–Mapper 三层架构开发 EMP 员工管理接口
4. 使用 Git 保留分阶段开发轨迹，为 ApiFox 接口测试打下基础

## 📁 项目结构
exp02-emp-mis
├── sql/init.sql              # 数据库初始化脚本（建库、建表、索引、初始数据）
├── src/main/java/com/lab/emp
│   ├── common                # Result 统一返回封装、BusinessException、GlobalExceptionHandler
│   ├── config                # SpringConfig、SpringMvcConfig、MyBatisPlusConfig、WebInitConfig
│   ├── controller            # EmpController（RESTful 接口）、AuthController（登录/退出）
│   ├── dto                   # EmpQueryDTO（列表多条件查询）、LoginForm（登录参数）
│   ├── entity                # Emp、Department
│   ├── interceptor           # LoginInterceptor（会话校验）
│   ├── mapper                # EmpMapper、DeptMapper（继承 BaseMapper）
│   └── service               # EmpService、DeptService 及实现
├── src/main/resources        # jdbc.properties、logback.xml
├── src/test/java             # 整合验证与接口测试（MockMvc）
└── pom.xml

## ▶ 运行步骤
1. 执行 `sql/init.sql` 初始化数据库（已沿用实验一 ssm_emp 库时可直接跳过）
2. 按本机情况修改 `src/main/resources/jdbc.properties` 的账号密码
3. Maven 刷新依赖后执行 `mvn clean package`，将 `target/emp-mis.war` 部署到 Tomcat 9
4. 启动后先调用 `POST /auth/login`（账号为 emp 表在岗员工姓名，演示口令 `emp@2026`），会话建立后再用 ApiFox 调试 `/emps` 系列接口；`POST /auth/logout` 退出，`GET /auth/me` 查看当前登录人

## 🕒 Commit 提交时间线
1. commit1（11-12）：初始化 Maven Web 工程，统一 Spring / MP / Druid 版本与 war 打包、UTF-8 编码，完善 .gitignore 与 IDE 配置
2. commit2（11-15）：搭建 Spring 根容器——Druid 连接池、MybatisSqlSessionFactoryBean、Mapper 扫描、声明式事务、MP 分页插件，补齐 jdbc 与日志配置
3. commit3（11-16）：完成 Web 层初始化与三层骨架——SpringMVC 子容器、跨域、UTF-8 过滤器，Emp/Department 实体、Mapper、Service 与统一返回，并通过整合验证用例
4. commit4（11-22）：数据库精细化设计——部门表与员工表脚本、emp_name/dept_id/status 索引、字段约束与自拟测试数据
5. commit5（11-22）：RESTful 员工接口与接口测试——/emps 增删改查、分页参数校验，补充 README 与提交记录
6. commit6（11-23）：多条件分页查询——新增 EmpQueryDTO，Service 用 Lambda 条件构造器按需拼接姓名模糊、性别/部门精确与薪资区间条件，Controller 一键接入并补充接口测试
7. commit7（11-23）：参数校验与全局异常——Emp 增加 JSR-303 约束（姓名、性别、薪资、状态等），新增/修改接口启用 @Valid，新增 GlobalExceptionHandler 统一处理校验失败、JSON 解析错误与兜底异常
8. commit8（11-24）：登录拦截与认证接口——新增 LoginInterceptor 以 Session 中的员工编号校验受保护请求（返回真实 401 + 统一 JSON），新增 /auth/login、/auth/logout、/auth/me，登录账号取自 emp 表在岗员工，并补充认证与拦截测试

## 🤖 AI 辅助使用记录
AI 用于依赖清单生成、配置类初稿、接口模板与报错分析；人工校验重点：
- MP 依赖误用 `mybatis-plus-boot-starter`（SSM 非 Spring Boot，改为 `mybatis-plus` + `mybatis-spring`）
- 分页拦截器仅声明 Bean 不生效，必须在 SqlSessionFactory 中 `setPlugins`
- 实体关联属性 `department` 需加 `@TableField(exist = false)`，否则拼入 SQL 报 Unknown column

## ✅ 当前进度
已完成实验二任务 2–5，以及任务 6 的参数校验、全局异常与登录拦截部分：项目初始化与依赖管理、SSM 全注解整合、数据库设计与分层代码、RESTful 接口与多条件分页、JSR-303 校验与统一异常返回、Session 登录认证与拦截器。
待后续提交：任务 6 剩余（AOP 请求日志、声明式事务验证）、任务 7（ApiFox 测试与项目复盘）。
