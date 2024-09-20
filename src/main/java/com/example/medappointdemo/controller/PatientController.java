package com.example.medappointdemo.controller;


import com.amazonaws.HttpMethod;
import com.example.medappointdemo.repository.UserRepository;
import com.example.medappointdemo.model.*;
import com.example.medappointdemo.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/patients")
public class PatientController {


    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private S3Service s3Service;

    @ModelAttribute
    public void addCommonAttributes(Principal principal,Model model) {

        String email = principal.getName();
        User user = userRepository.findByEmail(email);
        model.addAttribute("user", user);

        String photo = user.getPhoto();
        if(photo != null && !photo.isEmpty() ) {
            String resignedUrl = s3Service.generateUrl(photo, HttpMethod.GET);
            System.out.println(resignedUrl);
            model.addAttribute("imgUrl", resignedUrl);
        } else {
            model.addAttribute("imgUrl", "");
        }


        String viewAppsLink = "/patients/viewappointments/";
        String makeAppLinke = "/appointment";
        String patientId = user.getId().toString();
        viewAppsLink = viewAppsLink + patientId;
        makeAppLinke = "/patients/" + patientId + makeAppLinke;
        Map<String, String> controllerLinks = new LinkedHashMap<>();
        controllerLinks.put("View appointment(s)", viewAppsLink);
        controllerLinks.put("Make appointment", makeAppLinke);
        model.addAttribute("controllerLinks", controllerLinks);

        ArrayList<String> defaultLinks = new ArrayList<>();
        defaultLinks.add("Patients Home");
        defaultLinks.add("/patients/");
        String editInformationLink = "/edit";

        model.addAttribute("defaultLinks", defaultLinks);
        model.addAttribute("editInformationLink", editInformationLink);
    }

    @GetMapping({"/", "/index", "/?continue",""})
    public String viewPatientHomePage(Model model) {
        return "patient-home";
    }


    @GetMapping("/viewappointments/{id}")
    public String viewPatientAppointments(@PathVariable Long id, Model model) {
        List<Appointment> appointments = patientService.getAppointmentsForPatient(id);
        model.addAttribute("appointments", appointments);
        return "patient-viewappointments";
    }

    @GetMapping("/{id}/appointment")
    public String viewAvailableAppoints(@PathVariable Long id, Model model) {
        LocalDate today = LocalDate.now();
        LocalDate dueday = today.plusMonths(1);
        model.addAttribute("today", today.toString());
        model.addAttribute("dueday", dueday.toString());

        List<User> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);

        long daysBetween = ChronoUnit.DAYS.between(today.plusDays(1), dueday);
        model.addAttribute("daysBetween", daysBetween);

        Long doctorId = 2L; 
        List<Availability> generalAvailabilities = availabilityService.getAvailabilityForDoctor(doctorId);
        List<Availability> specialAvailabilities = availabilityService.getAvailabilityForDoctorWithDate(doctorId);

        ArrayList<DateTimeEntry> generalEntries = availableTimeOfDaysGeneral(generalAvailabilities, daysBetween, today);
        ArrayList<DateTimeEntry> specialEntries = availableTimeOfDaysSpecial(specialAvailabilities, daysBetween, today);

        ArrayList<DateTimeEntry> finalEntries = new ArrayList<>();
        for (DateTimeEntry generalEntry : generalEntries) {
            if (!specialEntries.contains(generalEntry)) {
                finalEntries.add(generalEntry);
            }
        }
        for (DateTimeEntry specialEntry : specialEntries) {
            if (specialEntry.getIsAvailable()) {
                finalEntries.add(specialEntry);
            }
        }

        
        finalEntries.sort(Comparator.comparing(DateTimeEntry::getDate)
                .thenComparing(DateTimeEntry::getTime));

        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String availabilitiesForDoctorJson = objectMapper.writeValueAsString(finalEntries);
            model.addAttribute("availabilitiesForDoctorJson", availabilitiesForDoctorJson);
            System.out.println(availabilitiesForDoctorJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("availabilitiesForDoctorJson", "[]");
        }


        return "patient-newappointment";
    }


    private ArrayList<DateTimeEntry> availableTimeOfDaysGeneral(List<Availability> availabilitiesForDoctorAjax, long daysBetween, LocalDate today) {
        ArrayList<DateTimeEntry> availabilitiesForDoctor = new ArrayList<>();
        //from day[0] - d[30] including 31 days
        for (int i = 0; i < daysBetween + 2; i++) {
            LocalDate currentDate = today.plusDays(i);
            int currentDayOfWeek = currentDate.getDayOfWeek().getValue();
            for (int j = 0; j < availabilitiesForDoctorAjax.size(); j++) {
                if (availabilitiesForDoctorAjax.get(j).getDayOfWeek() == currentDayOfWeek) {
                    LocalTime startTime = availabilitiesForDoctorAjax.get(j).getStartTime();
                    LocalTime endTime = availabilitiesForDoctorAjax.get(j).getEndTime();
                    boolean currentDateIsAvailable = availabilitiesForDoctorAjax.get(j).getIsAvailable();
                    int numberOfIntervals = endTime.getHour() - startTime.getHour();
                    long intervalMinutes = (endTime.toSecondOfDay() - startTime.toSecondOfDay()) / (numberOfIntervals) / 60;

                    LocalTime currentTime = startTime;
                    // build the available date and time list
                    for (int k = 0; k < numberOfIntervals; k++) {
                        availabilitiesForDoctor.add(new DateTimeEntry(currentDate, currentTime, currentDateIsAvailable));
                        currentTime = currentTime.plusMinutes(intervalMinutes);
                    }
                }
            }
        }
        return availabilitiesForDoctor;
    }


    private ArrayList<DateTimeEntry> availableTimeOfDaysSpecial(List<Availability> availabilitiesForDoctorAjax, long daysBetween, LocalDate today) {
        ArrayList<DateTimeEntry> availabilitiesForDoctor = new ArrayList<>();
        for (int i = 0; i < daysBetween + 2; i++) {
            LocalDate currentDate = today.plusDays(i);

            for (Availability forDoctorAjax : availabilitiesForDoctorAjax) {
                if (forDoctorAjax.getDate().equals(currentDate)) {
                    LocalTime startTime = forDoctorAjax.getStartTime();
                    LocalTime endTime = forDoctorAjax.getEndTime();
                    boolean currentDateIsAvailable = forDoctorAjax.getIsAvailable();
                    int numberOfIntervals = endTime.getHour() - startTime.getHour();
                    long intervalMinutes = (endTime.toSecondOfDay() - startTime.toSecondOfDay()) / (numberOfIntervals) / 60;

                    LocalTime currentTime = startTime;
                    // build the available date and time list
                    for (int k = 0; k < numberOfIntervals; k++) {
                        availabilitiesForDoctor.add(new DateTimeEntry(currentDate, currentTime, currentDateIsAvailable));
                        currentTime = currentTime.plusMinutes(intervalMinutes);
                    }
                }

            }
        }
        return availabilitiesForDoctor;
    }

    @PostMapping("/appointments/save")
    public String saveAppointment(
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("appointmentDate") String date,
            @RequestParam("appointmentTime") String time,
            Principal principal,
            Model model
    ) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDate appointmentDate = LocalDate.parse(date, dateFormatter);
            LocalTime appointmentTime = LocalTime.parse(time, timeFormatter);

            String email = principal.getName();

            User patient = patientService.getPatientByEmail(email);
            System.out.println("Patient is : " + patient);

            if (patient == null) {
                model.addAttribute("error", "未找到患者信息。");
                return "errorPage"; 
            }

            User doctor = doctorService.getDoctorById(Long.valueOf(doctorId));
            System.out.println("Doctor is : " + doctor);

            if (doctor == null) {
                model.addAttribute("error", "未找到医生信息。");
                return "errorPage"; 
            }

            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setStartTime(appointmentTime);
            appointment.setEndTime(appointmentTime.plusHours(1));
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

            appointmentService.saveAppointment(appointment);

            Availability availability = new Availability();
            availability.setDoctor(doctor);
            availability.setDate(appointmentDate);
            availability.setStartTime(appointmentTime);
            availability.setEndTime(appointmentTime.plusHours(1));
            availability.setIsAvailable(false);
            availability.setDayOfWeek(appointmentDate.getDayOfWeek().getValue());

            availabilityService.saveAvailability(availability);

            model.addAttribute("message", "您的预约已成功提交！");

            return "redirect:/patients/viewappointments/" + patient.getId();

        } catch (DateTimeParseException e) {
            model.addAttribute("error", "无效的日期或时间格式。");
            return "patient-newappointment"; 
        } catch (Exception e) {
            model.addAttribute("error", "预约过程中发生错误，请稍后重试。");
            return "patient-newappointment"; 
        }
    }

    @GetMapping("/availabilities")
    @ResponseBody
    public ResponseEntity<?> getAvailabilitiesForDoctor(@RequestParam Long doctorId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate dueday = today.plusMonths(1);
            long daysBetween = ChronoUnit.DAYS.between(today.plusDays(1), dueday);

            List<Availability> generalAvailabilities = availabilityService.getAvailabilityForDoctor(doctorId);
            List<Availability> specialAvailabilities = availabilityService.getAvailabilityForDoctorWithDate(doctorId);

            ArrayList<DateTimeEntry> generalEntries = availableTimeOfDaysGeneral(generalAvailabilities, daysBetween, today);
            ArrayList<DateTimeEntry> specialEntries = availableTimeOfDaysSpecial(specialAvailabilities, daysBetween, today);

            ArrayList<DateTimeEntry> finalEntries = new ArrayList<>();
            for (DateTimeEntry generalEntry : generalEntries) {
                if (!specialEntries.contains(generalEntry)) {
                    finalEntries.add(generalEntry);
                }
            }
            for (DateTimeEntry specialEntry : specialEntries) {
                if (specialEntry.getIsAvailable()) {
                    finalEntries.add(specialEntry);
                }
            }

            finalEntries.sort(Comparator.comparing(DateTimeEntry::getDate)
                    .thenComparing(DateTimeEntry::getTime));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); 

            String availabilitiesForDoctorJson = objectMapper.writeValueAsString(finalEntries);
            return ResponseEntity.ok(availabilitiesForDoctorJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("[]");
        }
    }


}


