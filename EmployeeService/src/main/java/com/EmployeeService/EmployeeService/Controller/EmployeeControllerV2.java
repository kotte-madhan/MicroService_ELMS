package com.elms.employeeservice.controller;

import com.elms.employeeservice.dto.EmployeeV2Response;
import com.elms.employeeservice.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/employees")
@RequiredArgsConstructor
public class EmployeeControllerV2 {

    private final EmployeeService employeeService;

    @GetMapping("/{id}")
    public EmployeeV2Response getEmployee(@PathVariable Long id) {
        return employeeService.getEmployeeByIdV2(id);
    }
}

