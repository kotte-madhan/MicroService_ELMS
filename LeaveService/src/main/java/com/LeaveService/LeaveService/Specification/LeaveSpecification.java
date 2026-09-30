package com.LeaveService.LeaveService.Specification;

import com.LeaveService.LeaveService.Entity.LeaveRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class LeaveSpecification {

    private LeaveSpecification() {}

    public static Specification<LeaveRequest> hasEmployeeId(Long employeeId) {
        return (root, query, cb) -> employeeId == null ? null : cb.equal(root.get("employeeId"), employeeId);
    }

    public static Specification<LeaveRequest> hasLeaveType(String leaveType) {
        return (root, query, cb) -> leaveType == null ? null : cb.equal(cb.lower(root.get("leaveType")), leaveType.toLowerCase());
    }

    public static Specification<LeaveRequest> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    public static Specification<LeaveRequest> startDateAfter(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("startDate"), date);
    }

    public static Specification<LeaveRequest> endDateBefore(LocalDate date) {
        return (root, query, cb) -> date == null ? null : cb.lessThanOrEqualTo(root.get("endDate"), date);
    }
}

