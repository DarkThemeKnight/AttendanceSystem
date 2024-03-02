package com.backend.FaceRecognition.services.authentication_service;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AuthenticationService {
    private final ApplicationUserService applicationUserService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(ApplicationUserService applicationUserService, JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.applicationUserService = applicationUserService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }
    /**
     * Registers a new application user with the provided details in a synchronized manner.
     * This method creates a new application user with the provided details, assigns the role of ROLE_LECTURER,
     * and saves the user using the application user service.
     * If the user creation is successful, it generates
     * a JWT token for the user and returns it along with a success response.
     * If the user already exists,
     * it returns a conflict response indicating that the user already exists.
     *
     * @param applicationUser The details of the application user to be registered.
     * @return A ResponseEntity containing an AuthenticationResponse object.
     *         If the user is successfully registered, a JWT token is returned along with a success response (200).
     *         If the user already exists, a conflict response (409) is returned along with an appropriate message.
     */
    public synchronized ResponseEntity<AuthenticationResponse> register(ApplicationUserRequest applicationUser) {
        ApplicationUser user = buildUser(applicationUser);
        user.setUserRole(Set.of(Role.ROLE_LECTURER));
        ResponseEntity<Void> response = applicationUserService.create(user);
        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return new ResponseEntity<>(new AuthenticationResponse("User already Exists", null), HttpStatus.CONFLICT);
        }
        String token = jwtService.generate(new HashMap<>(), user);
        return new ResponseEntity<>(new AuthenticationResponse("User Saved Successfully", token), HttpStatus.OK);
    }

    private ApplicationUser buildUser(ApplicationUserRequest applicationUser) {
        return ApplicationUser.builder()
                .id(applicationUser.getId())
                .firstname(applicationUser.getFirstname())
                .lastname(applicationUser.getLastname())
                .middleName(applicationUser.getMiddleName())
                .password(passwordEncoder.encode(applicationUser.getPassword()))
                .schoolEmail(applicationUser.getSchoolEmail())
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isAccountNonExpired(true)
                .isEnabled(true)
                .build();
    }
    /**
     * Authenticates a user based on the provided credentials.
     * This method attempts to authenticate a user using the provided credentials.
     * It first retrieves the user based on the provided ID from the application user service.
     * If the user is found, it checks if the account is enabled. If the account is enabled
     * and the provided password matches the user's password, it updates the user's credentials
     * expiration status, generates a JWT token for the user, and returns a success response with
     * the generated token. If the account is locked or the provided password is incorrect,
     * appropriate error responses are returned.
     *
     * @param request The authentication request containing the user ID and password.
     * @return A ResponseEntity containing an AuthenticationResponse object.
     *         If the login is successful, a JWT token is returned along with a success response (200).
     *         If the account is locked, a locked account response (423) is returned.
     *         If the user is not found or the password is incorrect, a not found response (404)
     *         or a conflict response (409) is returned, respectively.
     */
    public ResponseEntity<AuthenticationResponse> login(AuthenticationRequest request) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(request.getId());
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (!user.isEnabled()) {
                return new ResponseEntity<>(new AuthenticationResponse("Locked Account", null), HttpStatus.LOCKED);
            }
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                user.setCredentialsNonExpired(true);
                applicationUserService.update(user);
                String token = jwtService.generate(new HashMap<>(), user);
                return new ResponseEntity<>(new AuthenticationResponse("Login successfully", token), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new AuthenticationResponse("Invalid password", null), HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>(new AuthenticationResponse("User not found", null), HttpStatus.NOT_FOUND);
    }
    /**
     * Logs out a user based on the provided JWT token.
     * This method logs out a user based on the provided JWT token.
     * It first extracts the user ID from
     * the JWT token.
     * Then, it retrieves the user based on the extracted ID from the application user service.
     * If the user is found, it sets the user's credential expiration status too false to invalidate the token
     * and updates the user using the application user service.
     * Finally, it returns a success response indicating
     * that the logout was successful.
     * If the user is not found, a not found response is returned.
     *
     * @param bearerToken The JWT token provided in the Authorization header.
     * @return A ResponseEntity containing an AuthenticationResponse object.
     *         If the logout is successful, a success response (200) is returned.
     *         If the user is not found, a not found response (404) is returned.
     */
    public ResponseEntity<AuthenticationResponse> logout(String bearerToken) {
        String id = jwtService.getId(jwtService.extractTokenFromHeader(bearerToken));
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            user.setCredentialsNonExpired(false);
            applicationUserService.update(user);
            return new ResponseEntity<>(new AuthenticationResponse("Logout successful", null), HttpStatus.OK);
        }
        return new ResponseEntity<>(new AuthenticationResponse("User not found", null), HttpStatus.NOT_FOUND);
    }

}