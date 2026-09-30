package com.LeaveService.LeaveService.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double numberOfDays;

    private String reason;

    private String status;

    private Long approvedBy;

    private LocalDate approvedDate;

    private String remarks;

    @Version
    private Long version;
}