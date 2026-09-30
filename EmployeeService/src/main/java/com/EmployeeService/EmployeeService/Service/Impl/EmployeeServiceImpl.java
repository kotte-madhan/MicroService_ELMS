package com.EmployeeService.EmployeeService.Service.Impl;

import com.elms.employeeservice.client.DepartmentClient;
import com.elms.employeeservice.dto.EmployeeCreateRequest;
import com.elms.employeeservice.dto.EmployeeResponse;
import com.elms.employeeservice.dto.EmployeeUpdateRequest;
import com.elms.employeeservice.dto.EmployeeV2Response;
import com.elms.employeeservice.entity.Employee;
import com.elms.employeeservice.mapper.EmployeeMapper;
import com.elms.employeeservice.repository.EmployeeRepository;
import com.elms.employeeservice.service.EmployeeService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.elms.common.event.EmployeeCreatedEvent;
import com.elms.employeeservice.mapper.EmployeeEventMapper;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import com.elms.employeeservice.specification.EmployeeSpecification;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final DepartmentClient departmentClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:elms.events}")
    private String eventExchange;

    @Override
    @Retry(
            name = "departmentService",
            fallbackMethod = "departmentFallback")
    @CircuitBreaker(
            name = "departmentService",
            fallbackMethod = "departmentFallback")
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {

        departmentClient.getDepartment(request.getDepartmentId());

        com.EmployeeService.EmployeeService.Service.Impl.Employee employee = EmployeeMapper.toEntity(request);

        Employee saved = employeeRepository.save(employee);

        // Publish EmployeeCreated event via mapper to keep service DTOs independent from event contract
        try {
            var event = EmployeeEventMapper.toEmployeeCreatedEvent(saved);
            rabbitTemplate.convertAndSend(eventExchange, "employee.created.v2", event);
        } catch (Exception ex) {
            // Log and continue; publishing failure should not break creation
            // Use logger if available
        }

        return EmployeeMapper.toResponse(saved);
    }

    public EmployeeResponse departmentFallback(EmployeeCreateRequest request, Exception ex) {
        throw new RuntimeException("Department Service temporarily unavailable");
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest employeeDetails) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Optional: we could check version from request and compare
        try {
            EmployeeMapper.updateEntity(employee, employeeDetails);

            Employee saved = employeeRepository.save(employee);

            return EmployeeMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException ex) {
            throw new RuntimeException("Employee was modified by another user", ex);
        }
    }

    @Override
    public void deleteEmployee(Long id) {

        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return EmployeeMapper.toResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @Override
    public List<EmployeeResponse> searchEmployees(String name, String email, Long departmentId, String designation, String status, java.time.LocalDate joiningDateFrom, java.time.LocalDate joiningDateTo) {
        Specification<Employee> spec = Specification.where(EmployeeSpecification.hasName(name))
                .and(EmployeeSpecification.hasEmail(email))
                .and(EmployeeSpecification.hasDepartmentId(departmentId))
                .and(EmployeeSpecification.hasDesignation(designation))
                .and(EmployeeSpecification.hasStatus(status))
                .and(EmployeeSpecification.joiningDateAfter(joiningDateFrom))
                .and(EmployeeSpecification.joiningDateBefore(joiningDateTo));

        return employeeRepository.findAll(spec)
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeV2Response getEmployeeByIdV2(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Try to fetch department name via client; tolerate failures
        String deptName = null;
        try {
            var dept = departmentClient.getDepartment(e.getDepartmentId());
            if (dept != null) deptName = dept.getDepartmentName();
        } catch (Exception ex) {
            // ignore - versioned response may still include null
        }

        return EmployeeMapper.toV2Response(e, deptName);
    }
}