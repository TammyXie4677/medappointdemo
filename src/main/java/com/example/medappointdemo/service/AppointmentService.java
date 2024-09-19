package com.example.medappointdemo.service;

import com.example.medappointdemo.repository.AppointmentRepository;
import com.example.medappointdemo.model.Appointment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {


    @Autowired
    private AppointmentRepository appointmentRepository;

    public void saveAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);
    }



    }


