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
├── docs                      # 接口测试用例集、ApiFox 集合、架构说明、开发规范清单
├── src/main/java/com/lab/emp
│   ├── aspect                # RequestTraceAspect（Controller 层请求日志切面）
│   ├── common                # Result 统一返回封装、BusinessException、GlobalExceptionHandler
│   ├── config                # SpringConfig、SpringMvcConfig、MyBatisPlusConfig、WebInitConfig
│   ├── controller            # EmpController（RESTful 接口）、AuthController（登录/退出）
│   ├── dto                   # EmpQueryDTO（列表多条件查询）、LoginForm（登录参数）
│   ├── entity                # Emp、Department
│   ├── interceptor           # LoginInterceptor（会话校验）
│   ├── mapper                # EmpMapper、DeptMapper（继承 BaseMapper）
│   └── service               # EmpService、DeptService、EmpTxService（事务验证）及实现
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
9. commit9（11-24）：AOP 请求日志切面——@Around 拦截 Controller 层，为每次请求生成 traceId 写入 MDC，记录方法/URI/脱敏参数/耗时/响应状态，超过 500ms 记 WARN，异常记 ERROR 后原样抛出
10. commit10（11-24）：声明式事务验证——以部门批量迁移为载体，正常流程任一条员工缺失即抛业务异常并整体回滚；同时给出异常被吞、this 自调用、private 方法加注解三种失效场景，用测试对比观察数据是否残留
11. commit11（11-25）：任务 7 测试准备——新增 26 条接口测试用例清单与可导入 ApiFox 的 Postman 集合，补充登录→新增→单查→修改→条件列表→删除的全链路接口测试与非法性别校验用例
12. commit12（11-25）：前期实现复查与交付文档补齐——补 `PUT /emps` 主键校验、分页/薪资区间与单查单删编号合法性判断，新增查询参数类型错误与缺参的 400 处理，统一构造器注入与 Result 错误码注释，新增 `docs/architecture.md`（架构图+接口清单）、`docs/dev-standards.md`（14 条企业开发规范与复查结论），用例增至 31 个

## 🧪 测试与接口验证
- 用例清单：`docs/api-test-cases.md`（A 认证 6 条、E 员工 19 条、S 健壮性 4 条，共 29 条，标注自动化/待 ApiFox 执行）
- 接口集合：`docs/emp-mis.postman_collection.json`，ApiFox 可导入，变量 `baseUrl` 默认 `http://localhost:8080/emp-mis`
- 架构与接口：`docs/architecture.md`（分层架构图、请求链路、接口清单、数据模型、设计取舍）
- 规范清单：`docs/dev-standards.md`（14 条企业开发规范、本轮复查发现与结论）
- 自动化：`mvn test` 共 31 个用例（含 MockMvc 接口链路、事务回滚、登录拦截、切面日志、参数入口校验）
- 链路数据以「链路验证-」「事务验证-」命名并在用例结束后删除，库内不留测试残留

## 🔍 复查优化记录（commit12）
| 发现 | 风险 | 修正 |
|---|---|---|
| `PUT /emps` 不校验主键，`updateById` 失败仍返回成功 | 调用方误判更新生效 | 缺失或非法 `empId` 返回 400 |
| `?page=abc` 落到兜底分支返回 500 | 参数错误被误报为服务端故障 | 新增类型转换/缺参异常的 400 分支，提示具体参数名 |
| `minSalary > maxSalary` 仍查库 | 无效 SQL 往返，结果为空且无提示 | 控制层直接拦截并返回 400 |
| `GET/DELETE /emps/0` 未校验编号 | 无意义 SQL | 编号必须为正整数 |
| Controller 注入风格不一致 | 不利于替换依赖与测试 | 统一构造器注入 |

## 🧪 事务验证记录（任务 6.5）
| 场景 | 现象 | 原因 |
|---|---|---|
| `migrateDept` 加 `@Transactional(rollbackFor = Exception.class)` | 中途抛出 BusinessException 后，先更新的记录全部回滚 | 代理开启事务，异常触发回滚 |
| 异常被 try-catch 吞掉 | 方法返回 -1，已更新记录被保留 | 事务管理器感知不到异常，正常提交 |
| this 自调用同类事务方法 | 抛出异常但先更新的记录保留 | 自调用不走代理，注解不生效 |
| `@Transactional` 标在 private 方法 | 同上，数据不回滚 | 代理无法织入 private 方法，注解被忽略 |

## 🤖 AI 辅助使用记录
AI 用于依赖清单生成、配置类初稿、接口模板与报错分析；人工校验重点：
- MP 依赖误用 `mybatis-plus-boot-starter`（SSM 非 Spring Boot，改为 `mybatis-plus` + `mybatis-spring`）
- 分页拦截器仅声明 Bean 不生效，必须在 SqlSessionFactory 中 `setPlugins`
- 实体关联属性 `department` 需加 `@TableField(exist = false)`，否则拼入 SQL 报 Unknown column
- 仅引入 `spring-aspects` 不会让 `@Aspect` 生效，根容器必须显式 `@EnableAspectJAutoProxy` 并扫描 aspect 包
- 自调用、private 方法、异常被吞都会让 `@Transactional` 失效，事务验证必须通过代理方法入口调用

## ✅ 当前进度
已完成实验二任务 2–6 全部内容：项目初始化与依赖管理、SSM 全注解整合、数据库设计与分层代码、RESTful 接口与多条件分页、JSR-303 校验与统一异常返回、Session 登录认证与拦截器、Controller 层请求日志切面、声明式事务回滚与失效场景验证。
任务 7 已完成：测试用例集与 ApiFox 集合、全链路接口自动化验证、前期实现复查与参数入口加固、架构说明与接口清单、企业开发规范清单与复盘记录。
待后续提交：war 打包与 Tomcat 部署后的 ApiFox 全量执行记录、Git 收尾（发布 tag v1.0.0）。
