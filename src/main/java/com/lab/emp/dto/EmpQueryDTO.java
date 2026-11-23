package com.lab.emp.dto;

import java.math.BigDecimal;

/**
 * 员工列表查询条件。
 * <p>
 * 对应 GET /emps 的查询参数：姓名支持模糊匹配，性别与部门为精确匹配，
 * 薪资区间为可选扩展条件；所有字段均可为空，为空时不参与 SQL 条件拼接。
 */
public class EmpQueryDTO {

    /** 员工姓名，模糊匹配 */
    private String name;

    /** 性别，精确匹配 */
    private String gender;

    /** 部门名称，精确匹配 */
    private String dept;

    /** 薪资下限（含） */
    private BigDecimal minSalary;

    /** 薪资上限（含） */
    private BigDecimal maxSalary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.maxSalary = maxSalary;
    }
}
