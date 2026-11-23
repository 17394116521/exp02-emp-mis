package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.EmpService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {

    /**
     * 使用 Lambda 条件构造器按需拼接条件：
     * 姓名为空不拼 LIKE，性别/部门为空不拼等值，薪资区间为空不拼范围，
     * 最终固定按员工编号倒序，保证分页结果顺序稳定。
     */
    @Override
    public IPage<Emp> pageQuery(long page, long size, EmpQueryDTO query) {
        LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            String name = query.getName();
            wrapper.like(StringUtils.hasText(name), Emp::getEmpName, name)
                    .eq(StringUtils.hasText(query.getGender()), Emp::getGender, query.getGender())
                    .eq(StringUtils.hasText(query.getDept()), Emp::getDept, query.getDept());

            BigDecimal minSalary = query.getMinSalary();
            BigDecimal maxSalary = query.getMaxSalary();
            wrapper.ge(minSalary != null, Emp::getSalary, minSalary)
                    .le(maxSalary != null, Emp::getSalary, maxSalary);
        }
        wrapper.orderByDesc(Emp::getEmpId);
        return this.page(new Page<>(page, size), wrapper);
    }
}
