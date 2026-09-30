package com.EmployeeService.EmployeeService.Controller;

import com.elms.employeeservice.dto.EmployeeCreateRequest;
import com.elms.employeeservice.dto.EmployeeResponse;
import com.elms.employeeservice.dto.EmployeeUpdateRequest;
import com.elms.employeeservice.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public EmployeeResponse createEmployee(@RequestBody EmployeeCreateRequest request) {
        return employeeService.createEmployee(request);
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @GetMapping
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/search")
    public List<EmployeeResponse> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.time.LocalDate joiningDateFrom,
            @RequestParam(required = false) java.time.LocalDate joiningDateTo
    ) {
        return employeeService.searchEmployees(name, email, departmentId, designation, status, joiningDateFrom, joiningDateTo);
    }

    @PutMapping("/{id}")
    public EmployeeResponse updateEmployee(@PathVariable Long id, @RequestBody EmployeeUpdateRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteEmployee(
            @PathVariable Long id) {

        employeeService.deleteEmployee(id);

        return "Employee deleted successfully";
    }
}