package com.LeaveService.LeaveService.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {

    private Long id;

    private Long employeeId;

    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double numberOfDays;

    private String status;
}