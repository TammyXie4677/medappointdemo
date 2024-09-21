package com.example.medappointdemo.controller;

import com.amazonaws.HttpMethod;
import com.example.medappointdemo.model.Appointment;

import com.example.medappointdemo.model.User;
import com.example.medappointdemo.service.DoctorService;

import com.example.medappointdemo.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import javax.print.Doc;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/doctors")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private S3Service s3Service;

    @ModelAttribute
    public void addCommonAttributes(Principal principal,Model model) {

        String email = principal.getName();
        Optional<User> Doctor = doctorService.getDoctorByEmail(email);
        if (Doctor.isPresent()) {
            model.addAttribute("user", Doctor.get());

            String photo = Doctor.get().getPhoto();
            if (photo != null && !photo.isEmpty()) {
                String resignedUrl = s3Service.generateUrl(photo, HttpMethod.GET);
                System.out.println(resignedUrl);
                model.addAttribute("imgUrl", resignedUrl);
            } else {
                model.addAttribute("imgUrl", " ");  //considering a default avatar for all doctors
            }

            String viewAppsLink = "/doctors/appointments";
            String afterAppLink = "/doctors/afterAppointment";
            String applyCancelAppLink = "/doctors/applyCancelAppointment";
            String applyAvailabilityLink = "/doctors/applyAvailability";

            Map<String, String> controllerLinks = new LinkedHashMap<>();
            controllerLinks.put("View my appointment(s)", viewAppsLink);
            controllerLinks.put("Handler After appointment", afterAppLink);
            model.addAttribute("controllerLinks", controllerLinks);
        }
    }

    @GetMapping("/appointments")
    public String viewDoctorAppointments(Principal principal, Model model) {
        String email = principal.getName();
        List<Appointment> appointments = doctorService.getAppointmentsForDoctorByEmail(email);
        model.addAttribute("appointments", appointments);
        return "doctor-appointments";
    }

    @GetMapping({"/", "/index", "/?continue",""})
    public String viewDoctorHomepage(Principal principal, Model model){

            return "doctor-homepage";
    }
}
