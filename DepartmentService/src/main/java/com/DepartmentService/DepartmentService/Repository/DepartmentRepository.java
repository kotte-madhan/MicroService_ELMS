package com.DepartmentService.DepartmentService.Repository;

import com.DepartmentService.DepartmentService.Entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    boolean existsByDepartmentName(
            String departmentName);

    Optional<Department> findByDepartmentName(
            String departmentName);
}