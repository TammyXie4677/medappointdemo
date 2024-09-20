package com.example.medappointdemo.service;

import com.example.medappointdemo.repository.AppointmentRepository;
import com.example.medappointdemo.repository.AvailabilityRepository;
import com.example.medappointdemo.repository.AdminRepository;
import com.example.medappointdemo.model.Appointment;

import com.example.medappointdemo.model.Availability;
import com.example.medappointdemo.model.User;
import com.example.medappointdemo.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private AvailabilityRepository availabilityRepository;
    @Autowired
    private DoctorRepository doctorRepository;


    public User getAdminById(Long id) {

        return adminRepository.findAdminByUserId(id);
    }


    public User getAdminByEmail(String email) {

        return adminRepository.findAdminByEmail(email);
    }


    public List<Appointment> getAppointments() {

        List<Appointment> allAppointments = appointmentRepository.findAll();

        return allAppointments;
    }

    public List<Availability> getAvailabilities() {

        List<Availability> allAvailabilities = availabilityRepository.findAll();

        return allAvailabilities;
    }

    public List<User> getAllDoctors() {
        return adminRepository.findAllDoctors();
    }

    public List<Availability> getAvailabilitiesByDoctor(Long doctorId) {
        return adminRepository.findByDoctorIdOrderByDayOfWeekAndStartTimeGeneral(doctorId);
    }

    public List<Availability> getAllAvailabilitiesGroupedGeneral() {
        return adminRepository.findAllGroupedByDoctorAndDayOfWeekGeneral();
    }

    public List<Availability> getAvailabilitiesByDoctorSpecific(Long doctorId) {
        return adminRepository.findByDoctorIdOrderByDayOfWeekAndStartTimeSpecific(doctorId);
    }

    public List<Availability> getAllAvailabilitiesGroupedSpecific() {
        return adminRepository.findAllGroupedByDoctorAndDayOfWeekSpecific();
    }

    public Optional<Availability> getSepcificAvailability(Long id, LocalDate date,Integer dayOfWeek,LocalTime startTime, LocalTime endTime) {
        return availabilityRepository.findSpecificAvailability(id,date,dayOfWeek,startTime,endTime);
    }

    @Transactional
    public void deleteAvailabilityByIdGeneral(Long id) {
        adminRepository.deleteByIdWithoutDate(id);
    }

    @Transactional
    public void deleteAvailabilityByIdGSpecific(Long id) {
        adminRepository.deleteByIdWithDate(id);
    }

    public Optional<User> getDoctorByFirstNameAndLastName(String firstName, String lastName) {
        return doctorRepository.findDoctorByFirstNameAndLastName(firstName, lastName);
    }



}