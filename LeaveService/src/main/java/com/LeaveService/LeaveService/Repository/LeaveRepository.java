package com.LeaveService.LeaveService.Repository;

import com.LeaveService.LeaveService.Entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LeaveRepository
        extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    List<LeaveRequest> findByEmployeeId(
            Long employeeId);

    List<LeaveRequest> findByStatus(
            String status);
}