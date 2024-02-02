package com.unilorin.attendance_system.authentication_api.controller;

import com.unilorin.attendance_system.authentication_api.service.authentication.AuthenticationService;
import com.unilorin.attendance_system.authentication_api.utils.ApplicationUserRequest;
import com.unilorin.attendance_system.authentication_api.utils.AuthRequest;
import com.unilorin.attendance_system.authentication_api.utils.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/v1")
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerLecturer(@RequestBody ApplicationUserRequest applicationUser){
        return authenticationService.register(applicationUser);
    }
    @PostMapping("/register/admin")
    public ResponseEntity<String> register(@RequestBody ApplicationUserRequest applicationUser,@RequestHeader("Authorization") String bearer){
        log.info("token => {}",bearer);
        return authenticationService.register(applicationUser,bearer);
    }
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthRequest request) {
        return authenticationService.login(request);
    }
    @PutMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String bearerToken) {
        return authenticationService.logout(bearerToken);
    }



}
