package com.example.medappointdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {


    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) //for mysql server
    @GeneratedValue(strategy = GenerationType.SEQUENCE) //for postgresql
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // Reference to the doctor from the User entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient; // Reference to the patient from the User entity

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "notes")
    private String notes;

    @Column(name = "reason_for_visit")
    private String reasonForVisit;

    @Column(name = "is_follow_up")
    private Boolean isFollowUp = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;



    // Enum for Appointment Status
    public enum AppointmentStatus {
        SCHEDULED,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        RESCHEDULED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    // Getters and setters for the fields (omitted for brevity)
}