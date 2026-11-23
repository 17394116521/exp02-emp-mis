package com.lab.emp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Emp;

/**
 * 员工业务接口：通用 CRUD 由 IService 提供，这里补充带条件的列表查询。
 */
public interface EmpService extends IService<Emp> {

    /**
     * 多条件分页查询。
     *
     * @param page  页码，从 1 开始
     * @param size  每页条数
     * @param query 查询条件，可为 null 或字段为空
     * @return 分页结果，包含总记录数、总页数与当前页数据
     */
    IPage<Emp> pageQuery(long page, long size, EmpQueryDTO query);
}
