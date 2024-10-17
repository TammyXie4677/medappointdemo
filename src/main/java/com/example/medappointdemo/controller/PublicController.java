package com.example.medappointdemo.controller;


import com.amazonaws.HttpMethod;
import com.example.medappointdemo.repository.UserRepository;
import com.example.medappointdemo.model.Role;
import com.example.medappointdemo.model.User;
import com.example.medappointdemo.service.PatientService;
import com.example.medappointdemo.service.S3Service;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Slf4j
@Controller
public class PublicController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @GetMapping({"/home","/","/index",""})
    public String viewLoginPage(Model model) {
        return "public-home";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Principal principal, Model model) {
        if(principal != null){
            String role = (String) session.getAttribute("role");
            if(role!=null){
                switch (role) {
                    case "ROLE_ADMIN" -> {
                        return "redirect:/admins/";
                    }
                    case "ROLE_DOCTOR" -> {
                        return "redirect:/doctors/";
                    }
                    case "ROLE_PATIENT" -> {
                        return "redirect:/patients/";
                    }
                }
            }
        }

        return "login-form";
    }

    @GetMapping("/register")
    public String viewRegisterPage(HttpSession session, Principal principal, Model model){
        if(principal != null){
            String role = (String) session.getAttribute("role");
            if(role!=null){
                switch (role) {
                    case "ROLE_ADMIN" -> {
                        return "redirect:/admins/";
                    }
                    case "ROLE_DOCTOR" -> {
                        return "redirect:/doctors/";
                    }
                    case "ROLE_PATIENT" -> {
                        return "redirect:/patients/";
                    }
                }
            }
        }

        model.addAttribute("patient", new User());
        return "register-form";
    }

    @PostMapping("/register")
    public String processRegister(@Valid User user, BindingResult result, Model model){
        if (result.hasErrors()) {
            log.debug(String.valueOf(result));
            return "register-form";
        }
        if (!user.getPassword().equals(user.getPassword2())) {
            result.rejectValue("password2", "passwordsDoNotMatch", "Passwords must match");
            return "register-form";
        }
        if (patientService.getPatientByFirstNameAndLastName(user.getFirstName(),user.getLastName()) != null) {
            result.rejectValue("firstName", "usernameExists", "Username already exists");
            return "register-form";
        }
        if (patientService.getPatientByEmail(user.getEmail()) != null) {
            result.rejectValue("email", "emailExists", "Email already exists");
            return "register-form";
        }
        if(user.getRole() == null){
            user.setRole(Role.PATIENT);
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        user.setActive(true);
        patientService.savePatient(user);

        return "register-success";
    }

    @GetMapping("/edit")
    public String viewEditPage(Principal principal, RedirectAttributes redirectAttributes, Model model){
        if(principal == null){
            redirectAttributes.addFlashAttribute("message", "Please login first!");
            return("redirect:/login");
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        String photo = user.getPhoto();
        if (photo == null || photo.isEmpty()) {
            photo = "Patient_1726973794.png";
        }
        String resignedUrl = s3Service.generateUrl(photo, HttpMethod.GET);
        System.out.println(resignedUrl);
        model.addAttribute("imgUrl", resignedUrl);

        return "edit-form";
    }

    @PostMapping("/edit")
    public String processEdit(@RequestHeader(value = "referer", required = false) String referer,@Valid User user, BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model){
        if (result.hasErrors()) {
            return("redirect:/logout");
        }

        String email = principal.getName();
        User userInDb = userRepository.findByEmail(email);

        String phone = user.getPhone();
        userInDb.setPhone(phone);
        String photo  = user.getPhoto();
        userInDb.setPhoto(photo);
        String gender = user.getGender();
        userInDb.setGender(gender);

        userRepository.save(userInDb);
        redirectAttributes.addFlashAttribute("message", "Information updated successfully!");
        return "redirect:/patients/";
    }

    @GetMapping("/features")
    public String viewFeatures(Model model){
        return "features";
    }
}
