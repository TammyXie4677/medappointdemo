package com.example.medappointdemo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "availability")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Availability {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) //for mysql server
    @GeneratedValue(strategy = GenerationType.SEQUENCE) //for postgresql
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // Reference to the doctor from the User entity

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // to handle the null value

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Column(name = "date") // For special availability on specific dates
    private LocalDate date;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
}
