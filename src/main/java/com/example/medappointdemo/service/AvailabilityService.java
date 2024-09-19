package com.example.medappointdemo.service;

import com.example.medappointdemo.repository.AvailabilityRepository;

import com.example.medappointdemo.model.Availability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AvailabilityService {


    @Autowired
    private AvailabilityRepository availabilityRepository;


    public List<Availability> getAvailabilityForDoctor(Long id) {
        List<Availability> availabilitiesForDoctor = availabilityRepository.findAvailabilitiesByDoctorId(id);
        return availabilitiesForDoctor;
    }

    public List<Availability> getAvailabilityForDoctorWithDate(Long id) {
        List<Availability> availabilityForDoctorWithDate = availabilityRepository.findAvailabilitiesByDoctorIdWithDate(id);
        return availabilityForDoctorWithDate;
    }

    public void updateAvailability(Availability availability) {}

    public void saveAvailability(Availability availability) {
        availabilityRepository.save(availability);
    }

}