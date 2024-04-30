package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.utils.ResetPassword;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        // Delegate the authentication process to the AuthenticationService
        return authenticationService.login(authenticationRequest);
    }

    @PutMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization") String bearerToken) {
        // Delegate the logout process to the AuthenticationService
        return authenticationService.logout(bearerToken);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Response> forgotPassword(@RequestParam String userId){
        return authenticationService.forgotPassword(userId);
    }
    @PutMapping("/updatePassword/{token}")
    public ResponseEntity<Response> update(@PathVariable("token")String token, @RequestBody ResetPassword resetPassword) {
        return authenticationService.resetPassword(token, resetPassword);
    }

}
    