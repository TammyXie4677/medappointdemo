package com.example.medappointdemo.controller;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.medappointdemo.model.Appointment;

import com.example.medappointdemo.model.User;
import com.example.medappointdemo.repository.AppointmentRepository;
import com.example.medappointdemo.repository.PatientRepository;
import com.example.medappointdemo.service.DoctorService;

import com.example.medappointdemo.service.S3Service;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.print.Doc;
import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
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
    @Value("${S3_BUCKET_NAME}")
    private String bucketName;
    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PatientRepository patientRepository;


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
            String afterAppLink = "/doctors/afterappointment";
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


    @PostMapping("/editappointment/{id}")
    public String updateDoctorAppointment(@PathVariable("id") Long id,@Valid Appointment appointment,BindingResult result,RedirectAttributes redirectAttributes,Model model) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validation failed. Please correct the errors and try again.");
            return "redirect:/doctors/editappointment/{id}";
        }

        if (!appointment.getStatus().equals(Appointment.AppointmentStatus.COMPLETED) &&
                !appointment.getStatus().equals(Appointment.AppointmentStatus.SCHEDULED)) {
            redirectAttributes.addFlashAttribute("error", "Invalid status value");
            return "redirect:/error-page";
        }
        Appointment existingAppointment = appointmentRepository.findById(id).orElse(null);
        if (existingAppointment == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found");
            return "redirect:/error-page";
        }

        existingAppointment.setStatus(appointment.getStatus());
        appointmentRepository.save(existingAppointment);
        redirectAttributes.addFlashAttribute("success", "Appointment updated successfully");
        return "redirect:/doctors/appointments";
    }

    @GetMapping("/uploadFile/{id}")
    public String viewUpload(@PathVariable("id") Long id,Principal principal,RedirectAttributes redirectAttributes,Model model) {

        Appointment existingAppointment = appointmentRepository.findById(id).orElse(null);
        if (existingAppointment== null) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found");
            return "redirect:/error-page";
        }
        User patient = existingAppointment.getPatient();

        model.addAttribute("patient", patient);
        return "doctor-upload-form";
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    @PostMapping("/uploadFile/{id}")
    public String uploadPatientFile(@PathVariable("id") Long id, MultipartFile file, RedirectAttributes redirectAttributes, Model model) {


        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "File is empty");
            return "redirect:/error-page";
        }


        User patient = patientRepository.findById(id).orElse(null);
        if (patient == null) {
            redirectAttributes.addFlashAttribute("error", "Patient not found");
            return "redirect:/error-page";
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);


            if (!isValidFileExtension(fileExtension)) {
                redirectAttributes.addFlashAttribute("error", "Invalid file type");
                return "redirect:/error-page";
            }


            String contentType = file.getContentType();
            if (!isValidMimeType(contentType)) {
                redirectAttributes.addFlashAttribute("error", "Invalid MIME type");
                return "redirect:/error-page";
            }


            String newFileName = "Patient_" + Instant.now().getEpochSecond() + fileExtension;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucketName, newFileName, file.getInputStream(), metadata);


            patient.setMedicalHistory(newFileName);
            patientRepository.save(patient);

            redirectAttributes.addFlashAttribute("success", "Patient file uploaded successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "File upload failed");
        }

        return "redirect:/doctors/uploadFile/" +id;
    }

    // 文件扩展名校验
    private boolean isValidFileExtension(String fileExtension) {
        return fileExtension.equalsIgnoreCase(".pdf") ||
                fileExtension.equalsIgnoreCase(".txt") ||
                fileExtension.equalsIgnoreCase(".doc") ||
                fileExtension.equalsIgnoreCase(".docx");
    }

    // MIME 类型校验
    private boolean isValidMimeType(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.equals("text/plain") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }


    @GetMapping("/afterappointment")
    public String viewDoctorAfterAppointment(Principal principal, Model model) {
        String email = principal.getName();
        List<Appointment> appointments = doctorService.getCompletedAppointmentsForDoctorByEmail(email);
        model.addAttribute("appointments", appointments);
        return "doctor-after-appointments";
    }


    @GetMapping({"/", "/index", "/?continue",""})
    public String viewDoctorHomepage(Principal principal, Model model){
            return "doctor-homepage";
    }
}
