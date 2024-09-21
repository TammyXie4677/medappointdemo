package com.example.medappointdemo.controller;

import com.amazonaws.HttpMethod;
import com.example.medappointdemo.model.Appointment;

import com.example.medappointdemo.model.User;
import com.example.medappointdemo.service.DoctorService;

import com.example.medappointdemo.service.S3Service;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            String viewStatisticsLink = "/doctors/statistics";

            Map<String, String> controllerLinks = new LinkedHashMap<>();
            controllerLinks.put("View my appointment(s)", viewAppsLink);
            controllerLinks.put("Handler After appointment", afterAppLink);
            controllerLinks.put("my Statistics", applyCancelAppLink);
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

    @GetMapping("/editappointment/{id}")
    public String editDoctorAppointment(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes, Model model) {
        Appointment appointment = doctorService.getAppointmentForDoctor(id);

        if (appointment == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found");
            return "redirect:/error-page";
        }

        model.addAttribute("appointment", appointment);
        return "doctor-appointment-form";
    }

    @GetMapping({"/", "/index", "/?continue",""})
    public String viewDoctorHomepage(Principal principal, Model model){
            return "doctor-homepage";
    }
}
