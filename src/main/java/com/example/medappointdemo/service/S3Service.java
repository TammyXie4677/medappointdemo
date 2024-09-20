package com.example.medappointdemo.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.net.URL;

@Service
public class S3Service {


    @Autowired
    private AmazonS3 amazonS3;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    public String generateUrl(String filename, HttpMethod httpMethod) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 10);
        URL url = amazonS3.generatePresignedUrl(bucketName, filename, cal.getTime(), httpMethod);
        return url.toString();
    }


}

