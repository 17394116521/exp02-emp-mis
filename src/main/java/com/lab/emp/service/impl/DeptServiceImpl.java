package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.entity.Department;
import com.lab.emp.mapper.DeptMapper;
import org.springframework.stereotype.Service;

@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Department> implements IService<Department> {
}
