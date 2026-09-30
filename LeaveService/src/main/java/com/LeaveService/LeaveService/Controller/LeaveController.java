package com.LeaveService.LeaveService.Controller;

import com.LeaveService.LeaveService.DTO.LeaveResponse;
import com.LeaveService.LeaveService.Entity.LeaveRequest;
import com.LeaveService.LeaveService.Service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    public com.LeaveService.LeaveService.DTO.LeaveResponse applyLeave(@RequestBody com.LeaveService.LeaveService.DTO.LeaveCreateRequest request) {
        return leaveService.applyLeave(request);
    }

    @PutMapping("/{id}/approve")
    public com.LeaveService.LeaveService.DTO.LeaveResponse approveLeave(@PathVariable Long id, @RequestParam Long approverId) {
        return leaveService.approveLeave(id, approverId);
    }

    @PutMapping("/{id}/reject")
    public com.LeaveService.LeaveService.DTO.LeaveResponse rejectLeave(@PathVariable Long id, @RequestParam Long approverId) {
        return leaveService.rejectLeave(id, approverId);
    }

    @DeleteMapping("/{id}")
    public com.LeaveService.LeaveService.DTO.LeaveResponse cancelLeave(@PathVariable Long id) {
        return leaveService.cancelLeave(id);
    }

    @GetMapping("/{id}")
    public LeaveResponse getLeaveById(@PathVariable Long id) {
        return leaveService.getLeaveById(id);
    }

    @GetMapping("/employee/{employeeId}")
    public List<LeaveResponse> getEmployeeLeaves(@PathVariable Long employeeId) {
        return leaveService.getLeavesByEmployee(employeeId);
    }

    @GetMapping("/all")
    public List<LeaveResponse> getAllLeaves() {
        return leaveService.getAllLeaves();
    }

    @PostMapping("/bulk-approve")
    public java.util.Map<String, java.util.List<Long>> bulkApprove(@RequestBody java.util.List<Long> leaveIds, @RequestParam Long approverId) {
        return leaveService.bulkApprove(leaveIds, approverId);
    }

    @GetMapping("/search")
    public List<LeaveResponse> searchLeaves(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.time.LocalDate startDateFrom,
            @RequestParam(required = false) java.time.LocalDate endDateTo
    ) {
        return leaveService.searchLeaves(employeeId, leaveType, status, startDateFrom, endDateTo);
    }
}