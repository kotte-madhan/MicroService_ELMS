package com.elms.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Event published when a leave request is rejected
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LeaveRejectedEvent extends DomainEvent {

    @JsonProperty("leave_request_id")
    private String leaveRequestId;

    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("employee_email")
    private String employeeEmail;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("leave_type")
    private String leaveType;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("number_of_days")
    private Integer numberOfDays;

    @JsonProperty("rejected_by")
    private String rejectedBy;

    @JsonProperty("rejection_reason")
    private String rejectionReason;
}

