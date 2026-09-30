package com.elms.employeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeV2Response {
    private Long employeeId;
    private String fullName;
    private DepartmentSummary department;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentSummary {
        private Long id;
        private String name;
    }
}

