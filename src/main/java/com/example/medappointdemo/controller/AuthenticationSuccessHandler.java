package com.example.medappointdemo.controller;


import com.example.medappointdemo.repository.UserRepository;
import com.example.medappointdemo.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {


    private final UserRepository userRepository;

    @Autowired
    public AuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {


        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        System.out.println("Patient's Email is : " + email);

        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new ServletException("User not found");
        }

        // add the mail as user
        request.getSession().setAttribute("user", email);
        request.getSession().setAttribute("firstName", user.getFirstName());
        request.getSession().setAttribute("lastName", user.getLastName());

        String targetUrl = determineTargetUrl(authentication);
        setDefaultTargetUrl(targetUrl);        

        super.onAuthenticationSuccess(request, response, authentication);
    }

    protected String determineTargetUrl(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_DOCTOR") || role.equals("ROLE_PATIENT"))
                .findFirst()
                .map(role -> {
                    switch (role) {
                    case "ROLE_ADMIN":
                        return "/admins/";
                    case "ROLE_DOCTOR":
                        return "/doctors/";
                    case "ROLE_PATIENT":
                        return "/patients/";
                    default:
                        return "/home";
                    }
                })
                .orElse("/home");
    }


}

