package com.example.medappointdemo.repository;

import com.example.medappointdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PatientRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.role = 'PATIENT'")
    List<User> findAllPatients();

    List<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = 'PATIENT' and u.id=:id")
    Optional<User> findPatientByUserId(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.role = 'PATIENT' and u.firstName like :firstName and u.lastName like :lastName")
    User findPatientByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query("SELECT u FROM User u WHERE u.role = 'PATIENT' and u.email= :email")
    User findPatientByEmail(@Param("email") String email);

}
