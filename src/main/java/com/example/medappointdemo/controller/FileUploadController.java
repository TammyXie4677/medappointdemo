package com.example.medappointdemo.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
import java.util.HashMap;
import java.util.Map;

@Controller
public class FileUploadController {

    // Set this to the directory where you want to store the images
//    @Value("${upload.path}")
//    private String uploadDirectory;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    @Autowired
    private AmazonS3 amazonS3;



    @PostMapping("/uploadAvatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("imgUrl") MultipartFile file, Model model) {
        Map<String, String> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("msg", "File is empty. Please select a valid image.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Get the original file name and extension
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);

            // Generate new file name using timestamp
            String newFileName = "IMG_" + Instant.now().getEpochSecond() + fileExtension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucketName, newFileName, file.getInputStream(), metadata);

            // Path where the file will be saved
//            Path filePath = Paths.get(uploadDirectory + File.separator + newFileName);

            // Save the file
//            Files.write(filePath, file.getBytes());

            // Set the URL for the saved image (relative to the static directory)
//            String imgUrl = "/uploads/" + newFileName;
//            response.put("imgUrl", imgUrl);
//            response.put("msg", "File uploaded successfully!");
            response.put("imgUrl", newFileName);
//            response.put("imgUrl", amazonS3.getUrl(bucketName,newFileName).toString()); no! just the file name, not the full path and name.
            response.put("msg", "File uploaded successfully.");

            return ResponseEntity.ok(response);


        } catch (IOException e) {

            response.put("msg", "Error uploading file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }


    }

    // Utility method to get the file extension
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
