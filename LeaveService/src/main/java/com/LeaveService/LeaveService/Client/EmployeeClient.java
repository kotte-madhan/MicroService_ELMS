package com.LeaveService.LeaveService.Client;

import com.LeaveService.LeaveService.DTO.EmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "employee-service")
public interface EmployeeClient {

    @GetMapping("/api/v1/employees/{id}")
    EmployeeDTO getEmployee(
            @PathVariable Long id);
}