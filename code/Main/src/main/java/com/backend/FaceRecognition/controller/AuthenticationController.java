package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    /**
     * Registers a new user with the provided details.
     * This endpoint registers a new user with the provided details.
     * It delegates the registration process
     * to the AuthenticationService.
     * If the registration is successful, it returns a success response (200)
     * along with a JWT token.
     * If the user already exists, a conflict response (409) is returned along with
     * an appropriate message.
     *
     * @param applicationUser The details of the user to be registered.
     * @return A ResponseEntity containing an AuthenticationResponse object.
     *         If the registration is successful, a JWT token is returned along with a success response (200).
     *         If the user already exists, a conflict response (409) is returned along with an appropriate message.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody ApplicationUserRequest applicationUser) {
        return authenticationService.register(applicationUser);
    }

    /**
     * Authenticates a user based on the provided credentials.
     *
     * This endpoint authenticates a user based on the provided credentials.
     * It delegates the authentication process to the AuthenticationService.
     * If the login is successful, it returns a success response (200) along with a JWT token.
     * If the account is locked or the password is incorrect, appropriate error responses are returned.
     * If the user is not found, a not found response (404) is returned.
     *
     * @param authenticationRequest The authentication request containing the user ID and password.
     * @return A ResponseEntity containing an AuthenticationResponse object.
     *         If the login is successful, a JWT token is returned along with a success response (200).
     *         If the account is locked, a locked account response (423) is returned.
     *         If the password is incorrect, a conflict response (409) is returned.
     *         If the user is not found, a not found response (404) is returned.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        // Delegate the authentication process to the AuthenticationService
        return authenticationService.login(authenticationRequest);
    }

    /**
     * Logs out a user based on the provided JWT token.
     * This endpoint logs out a user based on the provided JWT token.
     * It delegates the logout process to the AuthenticationService.
     * If the logout is successful, it returns a success response (200).
     * If the user is not found, a not found response (404) is returned.
     *
     * @param bearerToken The JWT token provided in the Authorization header.
     * @return A ResponseEntity containing an AuthenticationResponse object.
     *         If the logout is successful, a success response (200) is returned.
     *         If the user is not found, a not found response (404) is returned.
     */
    @PutMapping("/logout")
    public ResponseEntity<AuthenticationResponse> logout(@RequestHeader(name = "Authorization") String bearerToken) {
        // Delegate the logout process to the AuthenticationService
        return authenticationService.logout(bearerToken);
    }

}
