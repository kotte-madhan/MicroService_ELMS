package com.elms.employeeservice.client;

import com.elms.employeeservice.dto.DepartmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "department-service")
public interface DepartmentClient {

    @GetMapping("/departments/{id}")
    DepartmentDTO getDepartment(
            @PathVariable Long id);
}