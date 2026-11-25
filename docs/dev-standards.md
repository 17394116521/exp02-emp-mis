# 企业开发规范清单与自检结论

本清单对应实验二任务 7 的交付物「企业开发规范清单（≥10 条）」。条目来自双师课堂讲解与项目实践，逐条给出本项目的落实情况、复查结论与证据位置。

## 一、规范清单

| 编号 | 规范条目 | 具体要求 | 本项目落实 | 结论 |
|---|---|---|---|---|
| R-01 | 分层职责单一 | Controller 只做协议转换与参数把关，Service 承载业务与事务，Mapper 只做数据访问 | `EmpController` 无业务分支；`EmpServiceImpl` 只组装条件；Mapper 继承 `BaseMapper` | 符合 |
| R-02 | 包结构按职责命名 | 按 `controller/service/mapper/entity/dto/common/config` 划分，禁止按人员或日期建包 | 现有包结构与职责一一对应 | 符合 |
| R-03 | 对外契约与实体分离 | 查询参数用 DTO，登录参数用独立表单对象，不把实体直接当入参 | `EmpQueryDTO`、`LoginForm` | 符合 |
| R-04 | 统一返回结构 | 所有接口返回 `code/message/data`，错误码在文档中枚举 | `Result` 注释已列出 200/400/401/404/500 | 符合 |
| R-05 | 异常不吞、出口唯一 | 业务异常显式抛出，系统异常统一兜底，堆栈只进日志 | `BusinessException` + `GlobalExceptionHandler` | 符合 |
| R-06 | 入参必须校验 | 实体用 JSR-303，查询参数在控制层做区间与类型把关 | 姓名/性别/薪资/状态/部门编号均有约束；分页与薪资区间单独判断 | 符合 |
| R-07 | 日志可追溯、可脱敏 | 每次请求有 traceId，口令类字段输出 `***`，慢请求告警 | `RequestTraceAspect`，阈值 500ms | 符合 |
| R-08 | 事务只加在公有入口 | 注解标在 Service 公有方法，明确 `rollbackFor`，禁止自调用与 private 注解 | `EmpTxServiceImpl#migrateDept` 为公有入口，其余失效场景仅用于对比验证 | 符合 |
| R-09 | SQL 可控 | 禁止 `SELECT *` 大字段扫描，条件查询走索引列，分页有上限 | 单表查询由 MP 生成列清单；三个索引；`size<=100` | 符合 |
| R-10 | 配置与代码分离 | 连接信息外置，口令不硬编码 | 连接信息在 `jdbc.properties` 且未入库；演示口令仍为常量 | 部分符合 |
| R-11 | 提交粒度与信息规范 | 一次提交一件事，前缀统一，信息说明“做了什么 + 怎么验证” | 11 次提交均为 `commitN: 内容 + 验证` | 符合 |
| R-12 | 不改写公共历史 | 已推送提交不回退重写，不强推 | 首次推送后仅快进推送；本轮提交沿用普通 `push` | 符合 |
| R-13 | 依赖版本集中管理 | 同一技术栈版本统一，禁止同一能力引入多套实现 | Spring 全家桶 5.3.31；MP + mybatis-spring，未引入 boot-starter | 符合 |
| R-14 | 测试与文档同步 | 行为变化同步补用例与接口文档，测试可一键复现 | 本轮新增 4 个用例并同步用例清单 | 符合 |

说明：R-10 的“部分符合”是已知取舍。课程环境用固定演示口令便于验收，正式项目应改为账号体系、口令加盐哈希、按角色授权，并在配置中心管理密钥。

## 二、本轮复查（对前期实现的再检查）

复查范围：`EmpController`、`GlobalExceptionHandler`、`Result` 注释与既有用例，逐项对照上面的规范与实际请求行为。

| 编号 | 复查发现 | 风险 | 处理 |
|---|---|---|---|
| C-01 | `PUT /emps` 不校验 `empId`，`updateById` 返回 `false` 仍返回 code 200 | 调用方误以为修改成功，数据实际未变 | 控制层增加主键合法性判断，缺失或 `<=0` 返回 400 |
| C-02 | `?page=abc` 触发 `MethodArgumentTypeMismatchException`，落到兜底分支返回 500 | 调用方参数写错却显示“服务端故障”，误导排查方向 | 全局异常处理新增该异常与缺参异常分支，返回 400 并指出参数名 |
| C-03 | `minSalary > maxSalary` 时仍执行查询，返回空列表且无提示 | 无效查询白白消耗一次数据库往返，问题不易发现 | 控制层比较区间，非法时直接 400 |
| C-04 | `GET /emps/0`、`DELETE /emps/0` 未校验主键 | 无意义 SQL；批量场景下易被误用 | 单查、单删补充 `id>0` 判断 |
| C-05 | `EmpController` 用字段注入，`AuthController` 用构造器注入，风格不一致 | 不利于单元测试替换依赖，规范不统一 | 统一改为构造器注入 |
| C-06 | `Result` 类注释只写“500 业务或系统失败”，与实际的 400/404 用法不一致 | 新成员按注释理解会误用错误码 | 注释改为完整错误码约定 |

复查同时确认了三处“看起来可疑但实际正确”的实现：`department` 已标 `@TableField(exist = false)` 不会进 SQL；分页插件已在 `SqlSessionFactory` 上注册；拦截器白名单包含 `/auth/**`，登录接口不会被自身拦截。

## 三、复查结论

- 本轮修正集中在“参数入口”，不改动业务语义，回归用例 31 个全部通过。
- 仍待改进：演示口令常量化（R-10）、`PUT` 语义更接近 `PATCH`（严格 REST 应拆分全量与局部更新）、war 部署与 ApiFox 全量执行属验收阶段工作。
- 后续提交延续“一次一件事”的粒度，提交信息继续采用 `commitN: 内容 + 验证` 格式，便于按时间线回溯。

## 四、对照实验：两处经典错误的复现与修正

为在报告中说明“错误长什么样、为什么错”，临时构造过两处故障并截图，随后全部回退。记录如下，截图仅作对照材料，不代表项目实际提交过缺陷代码。

| 对照项 | 构造方式 | 失败现象 | 根因 | 修正 |
|---|---|---|---|---|
| 事务自调用失效 | `migrateDept` 去掉注解，改为 `this.migrateDeptWithAnnotation(...)` 调用同类带注解方法 | `EmpTxServiceTest#rollbackWhenAnyEmpMissing` 失败：期望部门保持“原部门-验证”，实际已是“迁移部门-验证”，控制台可见先 UPDATE 后抛异常 | 自调用不经过 Spring 代理，`@Transactional` 未被织入，异常无法触发回滚 | 事务注解回到 `migrateDept` 公有入口，直接执行迁移逻辑 |
| 参数类型错误落到 500 | 注释掉 `GlobalExceptionHandler` 中 `MethodArgumentTypeMismatchException` 的处理分支 | `EmpControllerTest#nonNumericPageRejected` 失败：期望 `code=400`，实得 500，堆栈为 `NumberFormatException: For input string: "abc"` | 类型转换异常被兜底分支捕获，调用方参数错误被当成服务端故障 | 恢复该分支，单独返回 400 并指出参数名 `page` |
| IDE 将测试报文标红 | `malformedJsonRejected` 直接写字符串字面量 `{empName:}` | 编辑器按 JSON 语法检查报“JSON 标准不允许使用此类令牌”，用例本身却需要这段非法报文 | 该字面量故意非法，属于测试数据，不是代码缺陷 | 改为 `StringBuilder` 运行时拼接，既保留非法报文语义，又消除误标红 |

对照实验的临时改动未进入版本库；回退后执行 `mvn clean test`，31 个用例全部通过（Failures 0、Errors 0）。

