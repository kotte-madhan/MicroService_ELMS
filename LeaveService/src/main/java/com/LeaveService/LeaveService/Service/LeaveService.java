package com.LeaveService.LeaveService.Service;

import com.LeaveService.LeaveService.DTO.LeaveResponse;
import com.LeaveService.LeaveService.Entity.LeaveRequest;

import java.util.List;

public interface LeaveService {

    com.LeaveService.LeaveService.DTO.LeaveResponse applyLeave(com.LeaveService.LeaveService.DTO.LeaveCreateRequest leaveRequest);

    com.LeaveService.LeaveService.DTO.LeaveResponse approveLeave(Long leaveId, Long approverId);

    com.LeaveService.LeaveService.DTO.LeaveResponse rejectLeave(Long leaveId, Long approverId);

    com.LeaveService.LeaveService.DTO.LeaveResponse cancelLeave(Long leaveId);

    LeaveResponse getLeaveById(Long leaveId);

    List<LeaveResponse> getLeavesByEmployee(Long employeeId);

    List<LeaveResponse> getAllLeaves();

    // Bulk approve
    java.util.Map<String, java.util.List<Long>> bulkApprove(java.util.List<Long> leaveIds, Long approverId);

    // Search
    List<LeaveResponse> searchLeaves(Long employeeId, String leaveType, String status, java.time.LocalDate startDateFrom, java.time.LocalDate endDateTo);
}