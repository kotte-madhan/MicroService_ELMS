package com.LeaveService.LeaveService.Mapper;

import com.LeaveService.LeaveService.DTO.LeaveCreateRequest;
import com.LeaveService.LeaveService.DTO.LeaveResponse;
import com.LeaveService.LeaveService.Entity.LeaveRequest;

public class LeaveMapper {

    public static LeaveRequest toEntity(LeaveCreateRequest req) {
        return LeaveRequest.builder()
                .employeeId(req.getEmployeeId())
                .leaveType(req.getLeaveType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .reason(req.getReason())
                .build();
    }

    public static LeaveResponse toResponse(LeaveRequest e) {
        return new LeaveResponse(
                e.getId(),
                e.getEmployeeId(),
                e.getLeaveType(),
                e.getStartDate(),
                e.getEndDate(),
                e.getNumberOfDays(),
                e.getStatus()
        );
    }
}

