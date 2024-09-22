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

//        String continueUrl = request.getParameter("continue");
//        if (continueUrl != null && !continueUrl.isEmpty()) {
//            // Redirect to the continue URL if present
//            setDefaultTargetUrl(continueUrl);
//        } else
        if (authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            setDefaultTargetUrl("/admins/");
        } else if (authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DOCTOR"))) {
            setDefaultTargetUrl("/doctors/");
        } else if (authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_PATIENT"))) {
            setDefaultTargetUrl("/patients/");
        } else {
            setDefaultTargetUrl("/home");
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }




}

