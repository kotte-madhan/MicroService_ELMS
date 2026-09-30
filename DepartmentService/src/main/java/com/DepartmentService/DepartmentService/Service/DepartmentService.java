package com.DepartmentService.DepartmentService.Service;

import com.DepartmentService.DepartmentService.DTO.DepartmentResponse;
import com.DepartmentService.DepartmentService.Entity.Department;

import java.util.List;

public interface DepartmentService {

    Department createDepartment(
            Department department);

    Department updateDepartment(
            Long id,
            Department department);

    void deleteDepartment(
            Long id);

    DepartmentResponse getDepartmentById(
            Long id);

    List<DepartmentResponse> getAllDepartments();
}