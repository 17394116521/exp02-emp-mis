# EMP 管理 MIS 接口测试用例集（实验二 · 任务 7）

- 被测地址：`{{baseUrl}}`，默认 `http://localhost:8080/emp-mis`
- 认证方式：先调用 `POST /auth/login` 建立会话，同一会话内再访问受保护接口（工具需开启 Cookie 保持）
- 演示口令：`emp@2026`，账号为 emp 表中在岗员工姓名
- 统一返回结构：`{code, message, data}`，成功 200，参数/业务错误 400/404，未登录 401
- 说明：标「自动化通过」的用例已由 JUnit + MockMvc 覆盖，可在 `mvn test` 中复现；标「ApiFox 待执行」的用于部署后的手工/接口工具验证

## 一、认证与拦截（A）

| 编号 | 场景 | 请求 | 关键数据 | 预期结果 | 实测 |
|---|---|---|---|---|---|
| A-01 | 登录成功 | POST /auth/login | 在岗员工姓名 + emp@2026 | code=200，会话写入 loginEmpId | 自动化通过（AuthControllerTest） |
| A-02 | 口令错误 | POST /auth/login | 正确姓名 + wrong-pass | code=401，message=口令不正确，无登录态 | 自动化通过 |
| A-03 | 账号为空 | POST /auth/login | empName 空白 | code=400，提示登录账号不能为空 | ApiFox 待执行 |
| A-04 | 未登录访问员工接口 | GET /emps | 不带会话 | HTTP 401，code=401 | 自动化通过（EmpControllerTest） |
| A-05 | 退出后访问 | POST /auth/logout → GET /emps | 会话已销毁 | 退出 code=200；再次访问 401 | 自动化通过 |
| A-06 | 未登录查看身份 | GET /auth/me | 空会话 | code=401 | 自动化通过 |

## 二、员工 CRUD 与查询（E）

| 编号 | 场景 | 请求 | 关键数据 | 预期结果 | 实测 |
|---|---|---|---|---|---|
| E-01 | 默认分页 | GET /emps | 不传参数 | code=200，page=1、size=5，含 total/pages | 自动化通过 |
| E-02 | 分页边界 | GET /emps | page=0 | code=400，分页参数不合法 | 自动化通过 |
| E-03 | 多条件过滤 | GET /emps | name+gender+dept | code=200，仅返回匹配记录 | 自动化通过（total=0 分支） |
| E-04 | 薪资区间 | GET /emps | minSalary=8000,maxSalary=20000 | code=200，records 中薪资均在区间内 | 自动化通过 |
| E-05 | 单查存在 | GET /emps/{id} | 有效编号 | code=200，data 含 empName | 自动化通过 |
| E-06 | 单查不存在 | GET /emps/999999 | 无效编号 | code=200，data=null | 自动化通过（链路清尾验证） |
| E-07 | 新增成功 | POST /emps | 姓名+性别+部门+岗位+薪资+状态+deptId | code=200，列表可查回 | 自动化通过（EmpLifecycleApiTest） |
| E-08 | 新增空姓名 | POST /emps | empName="   " | code=400，员工姓名不能为空 | 自动化通过 |
| E-09 | 新增负薪资 | POST /emps | salary=-1 | code=400，薪资不能为负数 | 自动化通过 |
| E-10 | 新增非法性别 | POST /emps | gender=未知 | code=400，性别只能填写男或女 | 自动化通过 |
| E-11 | 非法 JSON | POST /emps | `{empName:}` | code=400，请求体格式错误 | 自动化通过 |
| E-12 | 修改成功 | PUT /emps | empId+姓名+新薪资 | code=200，单查薪资已变 | 自动化通过（EmpLifecycleApiTest） |
| E-13 | 单删 | DELETE /emps/{id} | 临时员工编号 | code=200，再查 data=null | 自动化通过（EmpLifecycleApiTest） |
| E-14 | 批量删除空数组 | DELETE /emps | `[]` | code=400，待删除的员工编号不能为空 | 自动化通过 |
| E-15 | 批量删除 | DELETE /emps | `[id1,id2]` | code=200，记录被移除 | ApiFox 待执行（涉及多写，手工验证后清理） |
| E-16 | 修改缺主键（复查补充） | PUT /emps | 仅姓名+薪资，无 empId | code=400，必须提供合法的 empId | 自动化通过 |
| E-17 | 薪资区间颠倒（复查补充） | GET /emps | minSalary=20000,maxSalary=8000 | code=400，薪资区间不合法 | 自动化通过 |
| E-18 | 分页参数非数字（复查补充） | GET /emps | page=abc | code=400，请求参数[page]格式不正确 | 自动化通过 |
| E-19 | 主键非法（复查补充） | GET /emps/0 | id=0 | code=400，员工编号必须为正整数 | 自动化通过 |

用例总数 29 条（A 6、E 19、S 4），其中 25 条已由 JUnit + MockMvc 自动化覆盖，4 条需部署后在 ApiFox 执行。

## 三、健壮性与工程化（S）

| 编号 | 场景 | 验证方式 | 预期结果 | 实测 |
|---|---|---|---|---|
| S-01 | 慢请求告警 | 观察控制台日志 | 耗时≥500ms 输出 WARN 并带 traceId | 自动化通过（AOP 切面日志） |
| S-02 | 参数脱敏 | 观察请求日志 | password/token 字段显示 `***` | 自动化通过（Aspect safeParams） |
| S-03 | 事务回滚 | EmpTxServiceTest | 中途失败先更新记录被回滚 | 自动化通过 |
| S-04 | 事务失效三场景 | EmpTxServiceTest | 吞异常/自调用/private 注解 → 数据残留 | 自动化通过 |

## 四、执行建议

1. ApiFox 新建项目，导入 `docs/emp-mis.postman_collection.json`（Postman Collection v2.1，ApiFox 可直接导入），设置环境变量 baseUrl。
2. 先在「认证」目录跑登录，确认 Cookie 已保存，再跑「员工管理」目录。
3. 失败用例重点看 message 文案与 HTTP 状态码是否同时符合预期（本系统 401 使用真实 HTTP 状态码）。
4. 用例产生的临时数据以「链路验证-」「事务验证-」开头，验证后手工删除，避免污染演示数据。
