package com.example.medappointdemo.controller;


import com.example.medappointdemo.repository.UserRepository;
import com.example.medappointdemo.model.*;
import com.example.medappointdemo.service.AppointmentService;
import com.example.medappointdemo.service.AvailabilityService;
import com.example.medappointdemo.service.DoctorService;
import com.example.medappointdemo.service.PatientService;
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

    @ModelAttribute
    public void addImgUrl(Principal principal,Model model) {

        String email = principal.getName();
        User user = userRepository.findByEmail(email);
        model.addAttribute("patient", user);

        String photo = user.getPhoto();
        if(photo.isEmpty() || photo == null) {
            photo = "";
        }
        model.addAttribute("imgUrl", photo);

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

        // 获取医生的可用时间段，根据需要替换医生ID
        Long doctorId = 2L; // 您可以根据需要设置默认医生ID
        List<Availability> generalAvailabilities = availabilityService.getAvailabilityForDoctor(doctorId);
        List<Availability> specialAvailabilities = availabilityService.getAvailabilityForDoctorWithDate(doctorId);

        ArrayList<DateTimeEntry> generalEntries = availableTimeOfDaysGeneral(generalAvailabilities, daysBetween, today);
        ArrayList<DateTimeEntry> specialEntries = availableTimeOfDaysSpecial(specialAvailabilities, daysBetween, today);

        // 合并一般和特殊的可用时间段
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

        // 对最终的可用时间段进行排序
        finalEntries.sort(Comparator.comparing(DateTimeEntry::getDate)
                .thenComparing(DateTimeEntry::getTime));

        // 序列化 finalEntries 并添加到模型中
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
            // 解析日期和时间
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDate appointmentDate = LocalDate.parse(date, dateFormatter);
            LocalTime appointmentTime = LocalTime.parse(time, timeFormatter);

            // 从 Principal 中获取用户名
            String email = principal.getName();

            // 根据用户名获取患者信息
            User patient = patientService.getPatientByEmail(email);
            System.out.println("Patient is : " + patient);

            if (patient == null) {
                model.addAttribute("error", "未找到患者信息。");
                return "errorPage"; // 替换为您的错误页面
            }

            // 根据医生 ID 获取医生信息
            User doctor = doctorService.getDoctorById(Long.valueOf(doctorId));
            System.out.println("Doctor is : " + doctor);

            if (doctor == null) {
                model.addAttribute("error", "未找到医生信息。");
                return "errorPage"; // 替换为您的错误页面
            }


            // 创建新的预约对象
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setStartTime(appointmentTime);
            appointment.setEndTime(appointmentTime.plusHours(1));
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

            // 保存预约信息
            appointmentService.saveAppointment(appointment);

            Availability availability = new Availability();
            availability.setDoctor(doctor);
            availability.setDate(appointmentDate);
            availability.setStartTime(appointmentTime);
            availability.setEndTime(appointmentTime.plusHours(1));
            availability.setIsAvailable(false);
            availability.setDayOfWeek(appointmentDate.getDayOfWeek().getValue());

            // 更新可预约表
            availabilityService.saveAvailability(availability);


            // 添加成功消息到模型
            model.addAttribute("message", "您的预约已成功提交！");

            // 返回预约确认页面
            return "redirect:/patients/viewappointments/" + patient.getId();

        } catch (DateTimeParseException e) {
            // 日期或时间格式错误，返回错误信息
            model.addAttribute("error", "无效的日期或时间格式。");
            return "patient-newappointment"; // 返回预约表单页面
        } catch (Exception e) {
            // 处理其他异常
            model.addAttribute("error", "预约过程中发生错误，请稍后重试。");
            return "patient-newappointment"; // 返回预约表单页面
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

            // 合并一般和特殊的可用时间段
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

            // 对最终的可用时间段进行排序
            finalEntries.sort(Comparator.comparing(DateTimeEntry::getDate)
                    .thenComparing(DateTimeEntry::getTime));

            // 序列化 finalEntries 并返回
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // 处理 Java 8 日期和时间类型
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 禁用时间戳

            String availabilitiesForDoctorJson = objectMapper.writeValueAsString(finalEntries);
            return ResponseEntity.ok(availabilitiesForDoctorJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("[]");
        }
    }


}


