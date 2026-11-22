package com.lab.emp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.emp.common.Result;
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

import java.util.List;

/**
 * 员工管理接口，遵循 RESTful 风格：
 * POST /emps 新增，PUT /emps 修改，DELETE /emps/{id} 单删，
 * DELETE /emps 批量删除，GET /emps/{id} 单查，GET /emps 分页列表。
 */
@RestController
@RequestMapping("/emps")
public class EmpController {

    @Autowired
    private EmpService empService;

    /** 新增员工 */
    @PostMapping
    public Result<Void> add(@RequestBody Emp emp) {
        empService.save(emp);
        return Result.ok();
    }

    /** 修改员工，按 empId 更新 */
    @PutMapping
    public Result<Void> update(@RequestBody Emp emp) {
        empService.updateById(emp);
        return Result.ok();
    }

    /** 删除单个员工 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
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
        return Result.ok(empService.getById(id));
    }

    /** 分页查询，page 从 1 开始 */
    @GetMapping
    public Result<IPage<Emp>> page(@RequestParam(defaultValue = "1") Long page,
                                   @RequestParam(defaultValue = "5") Long size) {
        if (page < 1 || size < 1 || size > 100) {
            return Result.fail(400, "分页参数不合法：page 最小为 1，size 取值 1~100");
        }
        return Result.ok(empService.page(new Page<>(page, size)));
    }
}
