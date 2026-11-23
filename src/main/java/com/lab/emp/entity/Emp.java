package com.lab.emp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工实体，对应 ssm_emp 库的 emp 表。
 * <p>
 * 注意：department 是关联查询用的对象属性，数据库中不存在该列，
 * 必须标注 {@code @TableField(exist = false)}，否则 MP 会把它拼进 SQL，
 * 报 Unknown column 'department' 错误。
 */
@TableName("emp")
public class Emp implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "emp_id", type = IdType.AUTO)
    private Integer empId;

    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "员工姓名长度不能超过 50 个字符")
    @TableField("emp_name")
    private String empName;

    @Pattern(regexp = "男|女", message = "性别只能填写男或女")
    private String gender;

    @Size(max = 50, message = "部门名称长度不能超过 50 个字符")
    private String dept;

    @Size(max = 50, message = "岗位名称长度不能超过 50 个字符")
    private String post;

    @NotNull(message = "薪资不能为空")
    @DecimalMin(value = "0", message = "薪资不能为负数")
    @Digits(integer = 8, fraction = 2, message = "薪资最多 8 位整数、2 位小数")
    private BigDecimal salary;

    private Date hireDate;

    @Min(value = 0, message = "在岗状态只能为 0 或 1")
    @Max(value = 1, message = "在岗状态只能为 0 或 1")
    private Integer status;

    @Min(value = 1, message = "部门编号必须为正整数")
    private Integer deptId;

    // 多对一关联对象，非数据库字段
    @TableField(exist = false)
    private Department department;

    public Emp(){}

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
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

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Emp{" +
                "empId=" + empId +
                ", empName='" + empName + '\'' +
                ", gender='" + gender + '\'' +
                ", dept='" + dept + '\'' +
                ", post='" + post + '\'' +
                ", salary=" + salary +
                ", hireDate=" + hireDate +
                ", status=" + status +
                ", deptId=" + deptId +
                ", department=" + department +
                '}';
    }
}
