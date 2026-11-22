-- =============================================================
-- 实验二 EMP 员工管理 MIS 系统 数据库初始化脚本
-- 库名：ssm_emp（沿用实验一）    字符集：utf8mb4
-- 说明：脚本可重复执行，不删除已有数据；已有 emp 表时会跳过建表，
--       如需补索引请单独执行文件末尾的 ALTER 语句。
-- =============================================================

CREATE DATABASE IF NOT EXISTS ssm_emp
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE ssm_emp;

-- -------------------------------------------------------------
-- 部门表（组织架构，与 emp 一对多）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS department (
    dept_id   INT PRIMARY KEY AUTO_INCREMENT COMMENT '部门主键',
    dept_name VARCHAR(50) NOT NULL COMMENT '组织名称',
    location  VARCHAR(50) DEFAULT NULL COMMENT '园区工位'
) ENGINE = InnoDB COMMENT = '组织架构部门表';

-- -------------------------------------------------------------
-- 员工表：emp_name 建普通索引支持模糊查询，dept_id 建普通索引支持关联
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS emp (
    emp_id    INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工主键',
    emp_name  VARCHAR(50) NOT NULL COMMENT '员工姓名',
    gender    CHAR(1) DEFAULT '男' COMMENT '性别标识',
    dept      VARCHAR(50) DEFAULT NULL COMMENT '所属组织（文本冗余，兼容实验一数据）',
    post      VARCHAR(50) DEFAULT NULL COMMENT '任职岗位',
    salary    DECIMAL(10,2) DEFAULT 0.00 COMMENT '月度薪资',
    hire_date DATE DEFAULT NULL COMMENT '到岗日期',
    status    TINYINT DEFAULT 1 COMMENT '在岗状态：1 在岗 0 离岗',
    dept_id   INT DEFAULT NULL COMMENT '关联部门主键',
    KEY idx_emp_name (emp_name),
    KEY idx_emp_dept_id (dept_id),
    KEY idx_emp_status (status)
) ENGINE = InnoDB COMMENT = '员工主数据表';

-- -------------------------------------------------------------
-- 初始化部门数据（自拟组织架构）
-- -------------------------------------------------------------
INSERT INTO department (dept_name, location) VALUES
('数智研发中心', '滨海园区 A 座 5 层'),
('增长运营中心', '滨海园区 B 座 3 层'),
('组织发展部',   '滨海园区 A 座 2 层'),
('财务管理部',   '滨海园区 B 座 4 层'),
('品牌创意部',   '滨海园区 C 座 1 层');

-- -------------------------------------------------------------
-- 初始化员工数据：自拟姓名与岗位，避免与指导书示例重合，
-- 薪资与入职时间错开，便于分页、排序与多条件查询演示
-- -------------------------------------------------------------
INSERT INTO emp (emp_name, gender, dept, post, salary, hire_date, status, dept_id) VALUES
('顾清晏', '男', '数智研发中心', '微服务架构师',   12800.00, '2024-09-02', 1, 1),
('裴无咎', '男', '数智研发中心', '算法工程师',     15600.00, '2023-07-24', 1, 1),
('温辞树', '女', '品牌创意部',   '视觉交互设计师', 10600.00, '2024-11-18', 1, 5),
('陆听澜', '女', '增长运营中心', '内容增长策划',    9200.00, '2022-05-09', 1, 2),
('沈砚书', '男', '组织发展部',   '人才发展专员',    8300.00, '2024-02-26', 0, 3),
('宋知微', '女', '财务管理部',   '成本核算会计',    9800.00, '2023-12-11', 1, 4);

-- -------------------------------------------------------------
-- 已有 emp 表时的手工补索引语句（按需执行，索引已存在会报 1061，可忽略）
-- -------------------------------------------------------------
-- ALTER TABLE emp ADD INDEX idx_emp_name (emp_name);
-- ALTER TABLE emp ADD INDEX idx_emp_dept_id (dept_id);
-- ALTER TABLE emp ADD INDEX idx_emp_status (status);
