package com.example.medappointdemo.repository;

import com.example.medappointdemo.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id= :id")
    List<Appointment> findAppointmentsByDoctor_Id(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id= :id")
    List<Appointment> findByPatientId(@Param("id") Long id);

    long countByPatient_Id(Long id);

    long countByPatient_Email(String email);
}
