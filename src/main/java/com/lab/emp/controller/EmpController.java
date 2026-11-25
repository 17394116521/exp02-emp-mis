package com.lab.emp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lab.emp.common.Result;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 员工管理接口，遵循 RESTful 风格：
 * POST /emps 新增，PUT /emps 修改，DELETE /emps/{id} 单删，
 * DELETE /emps 批量删除，GET /emps/{id} 单查，GET /emps 分页列表。
 * <p>
 * 控制层只做协议转换与参数把关：主键是否合法、分页区间是否合理、
 * 薪资上下限是否颠倒，这些判断放在这里可以避免无意义的 SQL 往返，
 * 也让错误提示更贴近调用方。
 */
@RestController
@RequestMapping("/emps")
public class EmpController {

    private final EmpService empService;

    @Autowired
    public EmpController(EmpService empService) {
        this.empService = empService;
    }

    /** 新增员工，实体上的 JSR-303 约束生效 */
    @PostMapping
    public Result<Void> add(@RequestBody @Valid Emp emp) {
        empService.save(emp);
        return Result.ok();
    }

    /** 修改员工：必须携带主键，否则 updateById 会静默返回 false，调用方误以为修改成功 */
    @PutMapping
    public Result<Void> update(@RequestBody @Valid Emp emp) {
        if (emp.getEmpId() == null || emp.getEmpId() <= 0) {
            return Result.fail(400, "修改员工时必须提供合法的 empId");
        }
        empService.updateById(emp);
        return Result.ok();
    }

    /** 删除单个员工 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "员工编号必须为正整数");
        }
        empService.removeById(id);
        return Result.ok();
    }

    /** 批量删除员工，请求体为 id 数组 */
    @DeleteMapping
    public Result<Void> deleteBatch(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail(400, "待删除的员工编号不能为空");
        }
        empService.removeByIds(ids);
        return Result.ok();
    }

    /** 按主键查询 */
    @GetMapping("/{id}")
    public Result<Emp> getById(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return Result.fail(400, "员工编号必须为正整数");
        }
        return Result.ok(empService.getById(id));
    }

    /** 分页 + 多条件查询：姓名模糊、性别/部门精确、薪资区间可选 */
    @GetMapping
    public Result<IPage<Emp>> page(@RequestParam(defaultValue = "1") Long page,
                                   @RequestParam(defaultValue = "5") Long size,
                                   EmpQueryDTO query) {
        if (page == null || page < 1 || size == null || size < 1 || size > 100) {
            return Result.fail(400, "分页参数不合法：page 最小为 1，size 取值 1~100");
        }
        if (query != null && query.getMinSalary() != null && query.getMaxSalary() != null
                && query.getMinSalary().compareTo(query.getMaxSalary()) > 0) {
            BigDecimal min = query.getMinSalary();
            BigDecimal max = query.getMaxSalary();
            return Result.fail(400, "薪资区间不合法：" + min + " 大于 " + max);
        }
        return Result.ok(empService.pageQuery(page, size, query));
    }
}
