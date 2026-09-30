package com.EmployeeService.EmployeeService.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            unique = true,
            nullable = false)
    private String employeeCode;

    @Column(
            nullable = false)
    private String name;

    @Column(
            unique = true,
            nullable = false)
    private String email;

    @Column(
            nullable = false)
    private String password;

    private String phone;

    /**
     * Department Service Reference
     */
    private Long departmentId;

    private String designation;

    private LocalDate joiningDate;

    private Integer leaveBalance;

    private String role;

    private String status;

    @Version
    private Long version;
}