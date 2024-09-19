package com.example.medappointdemo.service;

import com.example.medappointdemo.repository.AppointmentRepository;
import com.example.medappointdemo.model.User;
import lombok.extern.slf4j.Slf4j;
import com.example.medappointdemo.repository.PatientRepository;
import com.example.medappointdemo.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;


@Slf4j
@Controller
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    public User getPatientById(Long id) {

        return patientRepository.findPatientByUserId(id).orElse(null);
    }

    public List<Appointment> getAppointmentsForPatient(Long id) {

        List<Appointment> appointmentsForPatient = appointmentRepository.findByPatientId(id);

        return appointmentsForPatient;
    }


    public User getPatientByFirstNameAndLastName(String firstName, String lastName) {
        return patientRepository.findPatientByFirstNameAndLastName(firstName, lastName);
    }

    public User getPatientByEmail(String email) {
        return patientRepository.findPatientByEmail(email);
    }

    public void savePatient(User user) {
        patientRepository.save(user);
    }

    public long getPatientAppointmentsCount(long userId) {
        return appointmentRepository.countByPatient_Id(userId);
    }


}
