package com.example.medappointdemo.controller;


import com.example.medappointdemo.repository.UserRepository;
import com.example.medappointdemo.model.Role;
import com.example.medappointdemo.model.User;
import com.example.medappointdemo.service.PatientService;
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

    String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/static/uploads";


    @GetMapping({"/home","/","/index",""})
    public String viewLoginPage(Model model) {
        return "public-home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login-form"; // 这将返回 src/main/resources/templates/login-form.html
    }

    @GetMapping("/register")
    public String viewRegisterPage(Model model){
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
//            user.setRole(Role.PATIENT);
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

        return "edit-form";
    }

    @PostMapping("/edit")
    public String processEdit(@RequestHeader(value = "referer", required = false) String referer,@Valid User user, BindingResult result, Principal principal, RedirectAttributes redirectAttributes, Model model){
        if(principal == null){
            redirectAttributes.addFlashAttribute("message", "Please login first!");
            return("redirect:/login");
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



}
