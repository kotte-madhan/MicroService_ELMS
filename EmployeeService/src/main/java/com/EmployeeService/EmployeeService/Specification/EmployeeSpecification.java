package com.EmployeeService.EmployeeService.Specification;

import com.elms.employeeservice.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static <Employee> Specification<Employee> hasName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Employee> hasEmail(String email) {
        return (root, query, cb) -> email == null ? null : cb.equal(cb.lower(root.get("email")), email.toLowerCase());
    }

    public static Specification<Employee> hasDepartmentId(Long departmentId) {
        return (root, query, cb) -> departmentId == null ? null : cb.equal(root.get("departmentId"), departmentId);
    }

    public static Specification<Employee> hasDesignation(String designation) {
        return (root, query, cb) -> designation == null ? null : cb.equal(cb.lower(root.get("designation")), designation.toLowerCase());
    }

    public static Specification<Employee> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    public static Specification<Employee> joiningDateAfter(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("joiningDate"), date);
    }

    public static Specification<Employee> joiningDateBefore(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.lessThanOrEqualTo(root.get("joiningDate"), date);
    }
}

