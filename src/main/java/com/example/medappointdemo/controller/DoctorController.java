package com.example.medappointdemo.controller;

import com.example.medappointdemo.model.Appointment;

import com.example.medappointdemo.model.User;
import com.example.medappointdemo.service.DoctorService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/doctors")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;

    @GetMapping("/{id}/appointments")
    public String viewDoctorAppointments(@PathVariable Long id, Model model) {
        List<Appointment> appointments = doctorService.getAppointmentsForDoctor(id);
        model.addAttribute("appointments", appointments);
        return "doctor-appointments";
    }

    @GetMapping({"/", "/index", "/?continue",""})
    public String viewDoctorHomepage(Principal principal, Model model){
        String email = principal.getName();
        Optional<User> Doctor = doctorService.getDoctorByEmail(email);
        if(Doctor.isPresent()){
            model.addAttribute("doctor", Doctor.get());
            String toAppLink = "/appointments";
            String doctorId = Doctor.get().getId().toString();
            toAppLink = "/doctors/" + doctorId + toAppLink;

            model.addAttribute("toAppLink", toAppLink);
            return "doctor-homepage";
        } else {
            return "redirect:/logout";
        }


    }





}
