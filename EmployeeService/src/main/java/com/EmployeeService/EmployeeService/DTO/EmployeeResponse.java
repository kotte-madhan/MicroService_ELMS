package com.elms.employeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String name;
    private String email;
    private Long departmentId;
    private String designation;
    private Integer leaveBalance;
    private String role;
    private String status;
}