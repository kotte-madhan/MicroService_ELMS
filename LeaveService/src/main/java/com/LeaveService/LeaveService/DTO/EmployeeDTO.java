package com.LeaveService.LeaveService.DTO;

import lombok.Data;

@Data
public class EmployeeDTO {

    private Long id;

    private String employeeCode;

    private String name;

    private String email;

    private Long departmentId;
    private String status;
    private Integer leaveBalance;
}