package com.example.medappointdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) //for mysql server
    @GeneratedValue(strategy = GenerationType.SEQUENCE) //for postgresql
    private Long id;


//    @NotBlank(message="Username is required")
//    @Column(unique = true)
//    @Size(min = 6, max = 40, message= "Username must contain between {min} and {max} characters")
//    @Pattern(regexp = "^[a-z]+$", message = "Username must consist of lower case letters only")
    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Transient
    private String password2;

    @Column(name = "email",nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    @Column(name = "address", length = 255)
    private String address;


    @Column(name = "specialization")
    private String specialization;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "department")
    private String department;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "availability")
    private String availability;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "photo")
    private String photo;

    @Column(name = "status")
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false)
    private Role role;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "active")
    private boolean isActive;

    @OneToMany(mappedBy = "doctor")
    private Set<Availability> availabilities; // Only for doctors

    @OneToMany(mappedBy = "doctor")
    private Set<Appointment> doctorAppointments; // Appointments for doctors

    @OneToMany(mappedBy = "patient")
    private Set<Appointment> patientAppointments; // Appointments for patients


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}