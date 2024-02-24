package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody ApplicationUserRequest applicationUser) {
        return authenticationService.register(applicationUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return authenticationService.login(authenticationRequest);
    }

    @PutMapping("/logout")
    public ResponseEntity<AuthenticationResponse> logout(@RequestHeader(name = "Authorization") String bearerToken) {
        return authenticationService.logout(bearerToken);
    }
}
