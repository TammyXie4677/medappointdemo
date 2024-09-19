package com.example.medappointdemo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Controller
public class FileUploadController {

    // Set this to the directory where you want to store the images
    @Value("${upload.path}")
    private String uploadDirectory;

    @PostMapping("/uploadAvatar")
    public String uploadAvatar(@RequestParam("imgUrl") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("msg", "File is empty. Please select a valid image.");
            return "redirect:/edit";
        }

        try {
            // Get the original file name and extension
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);

            // Generate new file name using timestamp
            String newFileName = "IMG_" + Instant.now().getEpochSecond() + fileExtension;

            // Path where the file will be saved
            Path filePath = Paths.get(uploadDirectory + File.separator + newFileName);

            // Save the file
            Files.write(filePath, file.getBytes());

            // Set the URL for the saved image (relative to the static directory)
            String img = "/uploads/" + newFileName;
            model.addAttribute("msg", "File uploaded successfully!");
            model.addAttribute("imgUrl", img);


        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("msg", "Error uploading file: " + e.getMessage());
        }

        return "redirect:/edit"; // Adjust this based on your actual redirection
    }

    // Utility method to get the file extension
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
