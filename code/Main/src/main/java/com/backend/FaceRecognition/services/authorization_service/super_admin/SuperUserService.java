package com.backend.FaceRecognition.services.authorization_service.super_admin;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class SuperUserService {
    private final ApplicationUserService applicationUserService;

    public SuperUserService(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }
    public ResponseEntity<Response> setToAdmin(String id) {
        Optional<ApplicationUser> response = applicationUserService.findUser(id);
        if (response.isPresent()) {
            ApplicationUser user = response.get();
            boolean added = user.addUserRole(Role.ROLE_ADMIN);
            if (added) {
                applicationUserService.update(user);
                return new ResponseEntity<>(new Response("USER UPDATED TO ADMIN"), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Response("ALREADY AN ADMIN"), HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>(new Response("USER NOT FOUND"), HttpStatus.NOT_FOUND);
    }

}
