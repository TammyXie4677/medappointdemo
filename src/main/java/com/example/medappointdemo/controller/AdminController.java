package com.example.medappointdemo.controller;


import com.amazonaws.HttpMethod;
import com.example.medappointdemo.model.*;
import com.example.medappointdemo.repository.DoctorRepository;
import com.example.medappointdemo.repository.UserRepository;
import com.example.medappointdemo.service.AdminService;
import com.example.medappointdemo.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.medappointdemo.service.DoctorService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admins")
public class AdminController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminService adminService;
    @Autowired
    private AvailabilityService availabilityService;
    @Autowired
    private DoctorRepository doctorRepository;

    @ModelAttribute
    public void addCommonAttributes(Principal principal,Model model) {

        String email = principal.getName();
        User user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        String photo = "";
        model.addAttribute("imgUrl", photo);

        String patientId = user.getId().toString();

        String viewStatisticsLink = "/admins/statistics";
        String doctorRegistrationLink = "/admins/doctor-registration";
        String viewEmailLink = "/admins/emails";
        String viewAvailabilityLink = "/admins/availabilities";
        String viewAppoinetmentsLink = "/admins/appointments";
//        String adminId = user.getId().toString();
        Map<String, String> controllerLinks = new LinkedHashMap<>();
        controllerLinks.put("Statistics", viewStatisticsLink);
        controllerLinks.put("Doctor Registration", doctorRegistrationLink);
        controllerLinks.put("Email settings", viewEmailLink);
        controllerLinks.put("view Availabilities", viewAvailabilityLink);
        controllerLinks.put("view Appointments", viewAppoinetmentsLink);
        model.addAttribute("controllerLinks", controllerLinks);

        ArrayList<String> defaultLinks = new ArrayList<>();
        defaultLinks.add("Admin Home");
        defaultLinks.add("/admins/");
        String editInformationLink = "/edit";

        model.addAttribute("defaultLinks", defaultLinks);
        model.addAttribute("editInformationLink", editInformationLink);
    }


    @GetMapping({"/", "/index", "/?continue",""})
    public String index(Principal principal, Model model) {

        return "admin-home";
    }

    @GetMapping("/appointments")
    public String viewAdminAppointments(Model model) {
        List<Appointment> appointments = adminService.getAppointments();
        model.addAttribute("appointments", appointments);
        return "admin-appointments";
    }


    @GetMapping("/register")
    public String viewRegisterPage(Model model){
        model.addAttribute("doctor", new User());
        return "register-form";
    }

    @PostMapping("/register")
    public String processRegisterForm(@Valid User user, BindingResult result, Model model){
        return null;
    }

    @GetMapping("/statistics")
    public String viewStatistics(Model model){
        return "admin-statistics";
    }

    @GetMapping("/statistics/appointments")
    public String viewAppointments(Model model){
        return null;
    }

    public List<List<Object>> getAppointmentsByDate(List<Appointment> appointments) {
        Map<LocalDate, Integer> doctorAppointmentsCount = new HashMap<>();
        for (Appointment appointment : appointments) {
            LocalDate appointmentDate = appointment.getAppointmentDate();
            doctorAppointmentsCount.put(appointmentDate, doctorAppointmentsCount.getOrDefault(appointmentDate, 0) + 1);
        }
        List<List<Object>> appointmentList = new ArrayList<>();
        for(Map.Entry<LocalDate, Integer> entry : doctorAppointmentsCount.entrySet()) {
            List<Object> doctorData = new ArrayList<>();
            doctorData.add(entry.getKey());
            doctorData.add(entry.getValue());
            appointmentList.add(doctorData);
        }
        return appointmentList;
    }

    public List<List<Object>> getAppointmentsByDoctor(List<Appointment> appointments ){

        Map<String,Integer> doctorAppointmentCount = new HashMap<>();
        for(Appointment appointment : appointments){
            String doctorName = appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
            doctorAppointmentCount.put(doctorName, doctorAppointmentCount.getOrDefault(doctorName, 0) + 1);
        }
        List<List<Object>> appointmentList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : doctorAppointmentCount.entrySet()) {
            List<Object> doctorData = new ArrayList<>();
            doctorData.add(entry.getKey());
            doctorData.add(entry.getValue());
            appointmentList.add(doctorData);
        }
        return appointmentList;
    }

    @GetMapping("/statistics/doctors")
    public String viewByDoctors(Model model){
        List<Appointment> appointments = adminService.getAppointments();
        if(!appointments.isEmpty()) {
            List<List<Object>> getAppointmentsByDoctor = getAppointmentsByDoctor(appointments);
            model.addAttribute("getAppointmentsByDoctor", getAppointmentsByDoctor);
        }

        return "admin-statistics-by-doctors";
    }

    @GetMapping("/statistics/dates")
//    including charts
    public String viewByPatients(Model model){
        List<Appointment> appointments = adminService.getAppointments();
        if(!appointments.isEmpty()) {
            List<List<Object>> getAppointmentsByDoctor = getAppointmentsByDate(appointments);
            model.addAttribute("getAppointmentsByDoctor", getAppointmentsByDoctor);
        }

        return "admin-statistics-by-dates";
    }

    @GetMapping("/emails")
    public String viewEmail(Model model){
        return null;
    }

    @GetMapping("/emails/template")
    public String viewEmailTemplate(Model model){
        return null;
    }

    @GetMapping("/availabilities")
    public String viewAvailability(Principal principal,Model model){

        String email = principal.getName();
        User adminUser = userRepository.findByEmail(email);
        model.addAttribute("adminUser", adminUser);
        List<User> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);

        List<Availability> generalAvailabilities = adminService.getAllAvailabilitiesGroupedGeneral();
        if(!generalAvailabilities.isEmpty()) {
            Map<User, List<Availability>> availabilitiesGroupedByDoctorGeneral = generalAvailabilities.stream()
                    .collect(Collectors.groupingBy(Availability::getDoctor));
            model.addAttribute("availabilitiesGroupedByDoctorGeneral", availabilitiesGroupedByDoctorGeneral);
        }

        List<Availability> specificAvailable = adminService.getAllAvailabilitiesGroupedSpecific();
        if(!specificAvailable.isEmpty()){
            Map<User, List<Availability>> availabilitiesGroupedByDoctorSpecific = specificAvailable.stream()
                    .collect(Collectors.groupingBy(Availability::getDoctor));
            model.addAttribute("availabilitiesGroupedByDoctorSpecific", availabilitiesGroupedByDoctorSpecific);
        }
        return "admin-availabilities";
    }

    @GetMapping("/availabilities/newgeneral")
    public String showAvailabilityForm(Model model){
        Availability availability = new Availability();
        List<User> doctors = doctorService.getAllDoctors();
        model.addAttribute("availability", availability);
        model.addAttribute("doctors", doctors);

        return "admin-newgeneralavailability";
    }

    @GetMapping("/availabilities/newspecific")
    public String showSpecificAvailabilityForm(Model model){
        Availability availability = new Availability();
        List<User> doctors = doctorService.getAllDoctors();
        model.addAttribute("availability", availability);
        model.addAttribute("doctors", doctors);

        return "admin-newspecificavailability";
    }

    @GetMapping("/availabilities/maintenance")
    public String viewAvailabilityMaintenance(Model model){
        return null;
    }

    @PostMapping("/availabilities/savegeneral")
    public String processGeneralAvailability(@Valid Availability availability, BindingResult result,RedirectAttributes redirectAttributes, Model model){
        if(result.hasErrors()){
            model.addAttribute("availability", availability);
            return "admin-newgeneralavailability";
        }

        Long doctorId = availability.getDoctor().getId();
        LocalDate date = availability.getDate();
        Integer dayOfWeek = availability.getDayOfWeek();
        LocalTime startTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();
        Optional<Availability> specificAvailability = adminService.getSepcificAvailability(doctorId,date,dayOfWeek,startTime,endTime);
        if(specificAvailability.isEmpty())
        {
            availabilityService.saveAvailability(availability);
            redirectAttributes.addFlashAttribute("message", "Added a new schedule rule(general)!");
        }
        return "redirect:/admins/availabilities";
    }

    @PostMapping("/availabilities/savespecific")
    public String processsaceSpecificAvailability(@Valid Availability availability, BindingResult result,RedirectAttributes redirectAttributes, Model model){
        if(result.hasErrors()){
            redirectAttributes.addFlashAttribute("errorMessage", "There were errors in your form submission. Please fix them and try again.");
            model.addAttribute("availability", availability);
            return "admin-newspecificavailability";
        }

        Long doctorId = availability.getDoctor().getId();
        LocalDate date = availability.getDate();
        Integer dayOfWeek = availability.getDayOfWeek();
        LocalTime startTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();
        Optional<Availability> specificAvailability = adminService.getSepcificAvailability(doctorId,date,dayOfWeek,startTime,endTime);
        if(specificAvailability.isEmpty())
        {
            availabilityService.saveAvailability(availability);
            redirectAttributes.addFlashAttribute("message", "Added a new schedule rule(specific)!");
        }
        return "redirect:/admins/availabilities";
    }



    @GetMapping("/availabilities/deletegeneral/{id}")
    public String processAvailabilityDelete(@PathVariable Long id,RedirectAttributes redirectAttributes,Model model){

        adminService.deleteAvailabilityByIdGeneral(id);
        redirectAttributes.addFlashAttribute("message", "Availability deleted successfully!");

        return "redirect:/admins/availabilities";
    }

    @GetMapping("/doctor-registration")
    public String showRegistrationForm(Model model){
        List<User> doctors = doctorService.getAllDoctors();
        if(!doctors.isEmpty()) {
            model.addAttribute("doctors", doctors);
        }

        User newDoctor = new User();
        model.addAttribute("newDoctor", newDoctor);
        return "admin-doctor-form";
    }

    @PostMapping("/doctor-registration")
    public String processRegister(@ModelAttribute("newDoctor") User newDoctor, BindingResult result,RedirectAttributes redirectAttributes, Model model) {

        if (!newDoctor.getPassword().equals(newDoctor.getPassword2())) {
            result.rejectValue("password2", "passwordsDoNotMatch", "Passwords must match");
            return "admin-doctor-form";
        }

        Optional<User> doctorByFirstNameAndLastName = adminService.getDoctorByFirstNameAndLastName(newDoctor.getFirstName(), newDoctor.getLastName());
        if(doctorByFirstNameAndLastName.isPresent()){
            result.rejectValue("firstName", "usernameExists", "Username already exists");
            return "admin-doctor-form";
        }

        Optional<User> doctorByEmail = doctorService.getDoctorByEmail(newDoctor.getEmail());
        if (doctorByEmail.isPresent()) {
            result.rejectValue("email", "emailExists", "Email already exists");
            return "admin-doctor-form";
        }

        if(newDoctor.getRole() == null){
//            user.setRole(Role.PATIENT);
            newDoctor.setRole(Role.DOCTOR);
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newDoctor.getPassword());
        newDoctor.setPassword(encodedPassword);

        newDoctor.setActive(true);

        userRepository.save(newDoctor);
        redirectAttributes.addFlashAttribute("message", "Information updated successfully!");
        return "redirect:/admins/doctor-registration";

    }


}
