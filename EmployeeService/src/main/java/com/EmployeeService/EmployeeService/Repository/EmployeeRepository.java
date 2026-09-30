package com.elms.employeeservice.repository;

import com.elms.employeeservice.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface  EmployeeRepository
        extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(
            String email);

    Optional<Employee> findByEmployeeCode(
            String employeeCode);

    boolean existsByEmail(
            String email);

    boolean existsByEmployeeCode(
            String employeeCode);

    List<Employee> findByNameContainingIgnoreCase(
            String name);

    List<Employee> findByDepartmentId(
            Long departmentId);}