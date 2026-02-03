package com.laundry.controller;

import com.laundry.dto.JwtAuthenticationResponse;
import com.laundry.dto.LoginRequest;
import com.laundry.dto.SignupRequest;
import com.laundry.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        authService.registerUser(signupRequest);
        return ResponseEntity.ok("User registered successfully");
    }
}

