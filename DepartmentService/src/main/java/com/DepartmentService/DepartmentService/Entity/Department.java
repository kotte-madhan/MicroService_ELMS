package com.DepartmentService.DepartmentService.Entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String departmentName;

    private String description;
}