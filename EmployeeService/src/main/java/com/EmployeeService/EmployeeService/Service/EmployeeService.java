package com.elms.employeeservice.service;

import com.elms.employeeservice.dto.EmployeeCreateRequest;
import com.elms.employeeservice.dto.EmployeeResponse;
import com.elms.employeeservice.dto.EmployeeUpdateRequest;
import com.elms.employeeservice.dto.EmployeeV2Response;
import com.elms.employeeservice.entity.Employee;
import java.util.List;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);

    void deleteEmployee(Long id);

    EmployeeResponse getEmployeeById(Long id);

    List<EmployeeResponse> getAllEmployees();

    // V2 API
    EmployeeV2Response getEmployeeByIdV2(Long id);

    // Advanced search
    List<EmployeeResponse> searchEmployees(String name,
                                           String email,
                                           Long departmentId,
                                           String designation,
                                           String status,
                                           java.time.LocalDate joiningDateFrom,
                                           java.time.LocalDate joiningDateTo);
}