package com.elms.employeeservice.mapper;

import com.elms.employeeservice.dto.EmployeeCreateRequest;
import com.elms.employeeservice.dto.EmployeeResponse;
import com.elms.employeeservice.dto.EmployeeUpdateRequest;
import com.elms.employeeservice.dto.EmployeeV2Response;
import com.elms.employeeservice.entity.Employee;

public class EmployeeMapper {

    public static Employee toEntity(EmployeeCreateRequest req) {
        return Employee.builder()
                .employeeCode(req.getEmployeeCode())
                .name(req.getName())
                .email(req.getEmail())
                .password(req.getPassword())
                .phone(req.getPhone())
                .departmentId(req.getDepartmentId())
                .designation(req.getDesignation())
                .joiningDate(req.getJoiningDate())
                .leaveBalance(req.getLeaveBalance())
                .role(req.getRole())
                .status(req.getStatus())
                .build();
    }

    public static void updateEntity(Employee existing, EmployeeUpdateRequest req) {
        existing.setEmployeeCode(req.getEmployeeCode());
        existing.setName(req.getName());
        existing.setEmail(req.getEmail());
        existing.setPassword(req.getPassword());
        existing.setPhone(req.getPhone());
        existing.setDepartmentId(req.getDepartmentId());
        existing.setDesignation(req.getDesignation());
        existing.setJoiningDate(req.getJoiningDate());
        existing.setLeaveBalance(req.getLeaveBalance());
        existing.setRole(req.getRole());
        existing.setStatus(req.getStatus());
    }

    public static EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getEmployeeCode(),
                e.getName(),
                e.getEmail(),
                e.getDepartmentId(),
                e.getDesignation(),
                e.getLeaveBalance(),
                e.getRole(),
                e.getStatus()
        );
    }

    public static EmployeeV2Response toV2Response(Employee e, String departmentName) {
        EmployeeV2Response.DepartmentSummary dept = new EmployeeV2Response.DepartmentSummary(e.getDepartmentId(), departmentName);
        return new EmployeeV2Response(e.getId(), e.getName(), dept, e.getStatus());
    }
}

