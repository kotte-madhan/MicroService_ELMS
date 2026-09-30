package com.DepartmentService.DepartmentService.Controller;

import com.DepartmentService.DepartmentService.DTO.DepartmentResponse;
import com.DepartmentService.DepartmentService.Entity.Department;
import com.DepartmentService.DepartmentService.Service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public Department createDepartment(
            @RequestBody Department department) {

        return departmentService.createDepartment(
                department);
    }

    @GetMapping("/{id}")
    public DepartmentResponse getDepartmentById(
            @PathVariable Long id) {

        return departmentService.getDepartmentById(
                id);
    }

    @GetMapping
    public List<DepartmentResponse> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @PutMapping("/{id}")
    public Department updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department) {

        return departmentService.updateDepartment(
                id,
                department);
    }

    @DeleteMapping("/{id}")
    public String deleteDepartment(
            @PathVariable Long id) {

        departmentService.deleteDepartment(
                id);

        return "Department deleted successfully";
    }
}