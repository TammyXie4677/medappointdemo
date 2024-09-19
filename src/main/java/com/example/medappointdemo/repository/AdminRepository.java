package com.example.medappointdemo.repository;

import com.example.medappointdemo.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.medappointdemo.model.Availability;

import java.util.List;

public interface AdminRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.role= 'DOCTOR'")
    List<User> findAllDoctors();

    @Query("SELECT u FROM User u WHERE u.role= 'DOCTOR' AND u.email = :email")
    User findAdminByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.role= 'DOCTOR' AND u.id = :id")
    User findAdminByUserId(@Param("id") Long id);

    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctorId AND a.date IS NULL ORDER BY a.dayOfWeek, a.startTime")
    List<Availability> findByDoctorIdOrderByDayOfWeekAndStartTimeGeneral(Long doctorId);

    @Query("SELECT a FROM Availability a WHERE a.date IS NULL ORDER BY a.doctor.id, a.dayOfWeek, a.startTime")
    List<Availability> findAllGroupedByDoctorAndDayOfWeekGeneral();

    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctorId AND a.date IS NOT NULL ORDER BY a.dayOfWeek, a.startTime")
    List<Availability> findByDoctorIdOrderByDayOfWeekAndStartTimeSpecific(Long doctorId);

    @Query("SELECT a FROM Availability a WHERE a.date IS NOT NULL ORDER BY a.doctor.id, a.dayOfWeek, a.startTime")
    List<Availability> findAllGroupedByDoctorAndDayOfWeekSpecific();

    @Modifying
    @Transactional // 确保删除操作在事务中执行
    @Query("DELETE FROM Availability a WHERE a.date IS NULL AND a.id = :id")
    void deleteByIdWithoutDate(@Param("id") Long id);

    @Modifying
    @Transactional // 确保删除操作在事务中执行
    @Query("DELETE FROM Availability a WHERE a.date IS NOT NULL AND a.id = :id")
    void deleteByIdWithDate(@Param("id") Long id);


}
