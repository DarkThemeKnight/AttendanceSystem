package com.backend.FaceRecognition.services.authentication_service;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserDataPersistenceService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AuthenticationService {
    private final ApplicationUserDataPersistenceService applicationUserDataPersistenceService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(ApplicationUserDataPersistenceService applicationUserDataPersistenceService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.applicationUserDataPersistenceService = applicationUserDataPersistenceService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }
    public ResponseEntity<AuthenticationResponse> register(ApplicationUserRequest applicationUser) {
        ApplicationUser user = buildUser(applicationUser);
        user.setUserRole(Set.of(Role.ROLE_LECTURER));
        ResponseEntity<Void> response= applicationUserDataPersistenceService.create(user);
        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return new ResponseEntity<>(new AuthenticationResponse("User already Exists", null), HttpStatus.CONFLICT);
        }
        String token = jwtService.generate(new HashMap<>(),user);
        return new ResponseEntity<>(new AuthenticationResponse("User Saved Successfully",token),HttpStatus.OK);
    }
    public ApplicationUser buildUser(ApplicationUserRequest applicationUser) {
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
    public ResponseEntity<AuthenticationResponse> login(AuthenticationRequest request){
        Optional<ApplicationUser> userOptional = applicationUserDataPersistenceService.findUser(request.getId());
        if (userOptional.isPresent()){
            ApplicationUser user = userOptional.get();
            if (!user.isEnabled()){
                return new ResponseEntity<>(new AuthenticationResponse("Locked Account",null),HttpStatus.LOCKED);
            }
            if (passwordEncoder.matches(request.getPassword(),user.getPassword())){
                user.setCredentialsNonExpired(true);
                applicationUserDataPersistenceService.update(user);
                String token = jwtService.generate(new HashMap<>(),user);
                return new ResponseEntity<>(new AuthenticationResponse("Login successfully", token),HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(new AuthenticationResponse("Invalid password",null),HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>(new AuthenticationResponse("User not found",null),HttpStatus.NOT_FOUND);
    }
    public ResponseEntity<AuthenticationResponse> logout(String bearerToken){
        String id = jwtService.getId(jwtService.extractTokenFromHeader(bearerToken));
        Optional<ApplicationUser> userOptional = applicationUserDataPersistenceService.findUser(id);
        if (userOptional.isPresent()){
            ApplicationUser user = userOptional.get();
            user.setCredentialsNonExpired(false);
            applicationUserDataPersistenceService.update(user);
            return new ResponseEntity<>(new AuthenticationResponse("Logout successful",null),HttpStatus.OK);
        }
        return new ResponseEntity<>(new AuthenticationResponse("User not found",null),HttpStatus.NOT_FOUND);
    }


}