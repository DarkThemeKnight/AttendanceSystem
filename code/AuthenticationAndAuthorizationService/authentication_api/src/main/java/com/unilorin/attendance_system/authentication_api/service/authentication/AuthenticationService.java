package com.unilorin.attendance_system.authentication_api.service.authentication;

import com.unilorin.attendance_system.authentication_api.enumerations.Role;
import com.unilorin.attendance_system.authentication_api.exceptions.UserAlreadyExistsException;
import com.unilorin.attendance_system.authentication_api.repository_requests.ApplicationUserRequestService;
import com.unilorin.attendance_system.authentication_api.service.token_management.JwtService;
import com.unilorin.attendance_system.authentication_api.utils.ApplicationUser;
import com.unilorin.attendance_system.authentication_api.utils.ApplicationUserRequest;
import com.unilorin.attendance_system.authentication_api.utils.AuthRequest;
import com.unilorin.attendance_system.authentication_api.utils.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class AuthenticationService {
    private final ApplicationUserRequestService requestService;
    private final JwtService jwtService;
    @Autowired
    public AuthenticationService(ApplicationUserRequestService requestService, JwtService jwtService) {
        this.requestService = requestService;
        this.jwtService = jwtService;
    }
    public ResponseEntity<String> register(ApplicationUserRequest applicationUser, String bearer) {
        try {
            String jwt_token = jwtService.extractTokenFromHeader(bearer);
            String id = jwtService.extractID(jwt_token);
            log.info("Extracted ID: {}", id);
            ApplicationUser response = requestService.findUser(id);
            if (!response.getUserRole().contains(Role.ROLE_SUPER_ADMIN) && !response.getUserRole().contains(Role.ROLE_ADMIN)) {
                log.warn("Unauthorized access attempt for user: {}", id);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            List<Role> roles = applicationUser.getUserRole();
            if (roles == null) {
                roles = new ArrayList<>();
            }
            roles.add(Role.ROLE_ADMIN);
            applicationUser.setUserRole(roles);
            ApplicationUser userResponse = requestService.create(applicationUser);
            if (userResponse == null) {
                log.warn("User already exists: {}", applicationUser.getFirstname());
                return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
            }
            jwt_token = jwtService.generate(new HashMap<>(), userResponse);
            log.info("User created successfully: {}", applicationUser.getFirstname());
            return new ResponseEntity<>("User created successfully", HttpStatus.OK);
        } catch (UserAlreadyExistsException e) {
            log.warn("User already exists: {}", applicationUser.getFirstname());
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Error occurred during user registration", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public ResponseEntity<AuthenticationResponse> register(ApplicationUserRequest applicationUser) {
        try {
            List<Role> roles = new ArrayList<>();
            roles.add(Role.ROLE_LECTURER);
            applicationUser.setUserRole(roles);

            log.info("Attempting to create user: {}", applicationUser.getFirstname());

            ApplicationUser userResponse = requestService.create(applicationUser);
            if (userResponse == null) {
                log.warn("User creation failed - User already exists: {}", applicationUser.getFirstname());
                return new ResponseEntity<>(new AuthenticationResponse("User already exists", null), HttpStatus.CONFLICT);
            }

            String jwt_token = jwtService.generate(new HashMap<>(), userResponse);
            log.info("User created successfully: {}", applicationUser.getFirstname());
            return new ResponseEntity<>(new AuthenticationResponse("User created successfully", jwt_token), HttpStatus.OK);
        } catch (UserAlreadyExistsException e) {
            log.warn("User creation failed - User already exists: {}", applicationUser.getFirstname(), e);
            return new ResponseEntity<>(new AuthenticationResponse("User already exists", null), HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Error occurred during user registration for user: {}", applicationUser.getFirstname(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<AuthenticationResponse> login(AuthRequest request) {
        try {
            log.info("Attempting login for user: {}", request.getUserid());
            ApplicationUser userResponse = requestService.loginUser(request);
            if (userResponse == null) {
                log.warn("User does not exist: {}", request.getUserid());
                return new ResponseEntity<>(new AuthenticationResponse("User does not exist", null), HttpStatus.NOT_FOUND);
            }
            String jwt_token = jwtService.generate(new HashMap<>(), userResponse);
            log.info("User logged in successfully: {}", request.getUserid());
            return new ResponseEntity<>(new AuthenticationResponse("User logged in successfully", jwt_token), HttpStatus.OK);
        } catch (DisabledException e) {
            log.warn("User is disabled: {}", request.getUserid(), e);
            return new ResponseEntity<>(new AuthenticationResponse("User is disabled", null), HttpStatus.LOCKED);
        } catch (UsernameNotFoundException e) {
            log.warn("User does not exist: {}", request.getUserid(), e);
            return new ResponseEntity<>(new AuthenticationResponse("User does not exist", null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred during user login for user: {}", request.getUserid(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> logout(String bearerToken) {
        String jwt_token;
        String id= "";
        try {
            jwt_token = jwtService.extractTokenFromHeader(bearerToken);
            id = jwtService.extractID(jwt_token);
            log.info("Logging out user with ID: {}", id);
            return requestService.update("credentials", "false", bearerToken);
        } catch (Exception e) {
            log.error("Error occurred during logout for user with ID: {}", id);
            // Return an appropriate response in case of an error
            return new ResponseEntity<>("Error occurred during logout", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
