package com.example.medappointdemo.service;

import com.example.medappointdemo.repository.AppointmentRepository;
import com.example.medappointdemo.repository.DoctorRepository;
import com.example.medappointdemo.model.Appointment;

import com.example.medappointdemo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DoctorService {
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;


    public User getDoctorById(Long id) {

        return doctorRepository.findDoctorByUserId(id).orElse(null);
    }

    public Optional<User> getDoctorByEmail(String email) {

        return doctorRepository.findDoctorByEmail(email);
    }


    public List<Appointment> getAppointmentsForDoctor(Long id) {

        List<Appointment> appointmentsForDoctor = appointmentRepository.findAppointmentsByDoctor_Id(id);

        return appointmentsForDoctor;
    }

    public List<Appointment> getAppointmentsForDoctorByEmail(String email) {

        List<Appointment> appointmentsForDoctorByEmail = appointmentRepository.findAppointmentsByDoctor_Email(email);

        return appointmentsForDoctorByEmail;
    }

    public List<User> getAllDoctors() {
        return doctorRepository.findAllDoctors();
    }

    public Appointment getAppointmentForDoctor(Long id) {
        return appointmentRepository.findAppointmentById(id);
    }

    public List<Appointment> getCompletedAppointmentsForDoctorByEmail(String email) {
        return appointmentRepository.findCompletedAppointment_Email(email);
    }





}