package com.example.medappointdemo.repository;

import com.example.medappointdemo.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    // Custom query methods if needed
    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctorId and a.date IS NULL")
    List<Availability> findAvailabilitiesByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctorId and a.date IS NOT NULL")
    List<Availability> findAvailabilitiesByDoctorIdWithDate(@Param("doctorId") Long doctorId);

    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :id AND a.date = :date AND a.dayOfWeek= :dayOfWeek AND a.startTime = :startTime AND a.endTime = :endTime")
    Optional<Availability> findSpecificAvailability(@Param("id") Long id, @Param("date") LocalDate date, @Param("dayOfWeek") Integer dayOfWeek, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);


}