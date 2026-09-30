package com.DepartmentService.DepartmentService.Service.Impl;

import com.DepartmentService.DepartmentService.DTO.DepartmentResponse;
import com.DepartmentService.DepartmentService.Entity.Department;
import com.DepartmentService.DepartmentService.Repository.DepartmentRepository;
import com.DepartmentService.DepartmentService.Service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl
        implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department createDepartment(
            Department department) {

        return departmentRepository.save(
                department);
    }

    @Override
    public Department updateDepartment(
            Long id,
            Department departmentDetails) {

        Department department =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found"));

        department.setDepartmentName(
                departmentDetails.getDepartmentName());

        department.setDescription(
                departmentDetails.getDescription());

        return departmentRepository.save(
                department);
    }

    @Override
    public void deleteDepartment(
            Long id) {

        Department department =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found"));

        departmentRepository.delete(
                department);
    }

    @Override
    public DepartmentResponse getDepartmentById(
            Long id) {

        Department department =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found"));

        return mapToResponse(
                department);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private DepartmentResponse mapToResponse(
            Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentName(),
                department.getDescription());
    }
}