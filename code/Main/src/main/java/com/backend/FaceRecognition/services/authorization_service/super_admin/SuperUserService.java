package com.backend.FaceRecognition.services.authorization_service.super_admin;
import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.authorization_service.admin.AdminService;
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
    private final PasswordEncoder passwordEncoder;
    private final AdminService adminService;
    public SuperUserService(ApplicationUserService applicationUserService, PasswordEncoder passwordEncoder, AdminService adminService) {
        this.applicationUserService = applicationUserService;
        this.passwordEncoder = passwordEncoder;
        this.adminService = adminService;
    }
    public ResponseEntity<Response> addNewAdmin(ApplicationUserRequest request){
        ApplicationUser user = buildUser(request);
        user.setUserRole(Set.of(Role.ROLE_ADMIN));
        ResponseEntity<Void> response= applicationUserService.create(user);
        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(new Response("Admin added"), HttpStatus.OK);
    }
    public ResponseEntity<Response> setToAdmin(String id){
        Optional<ApplicationUser> response= applicationUserService.findUser(id);
        if (response.isPresent()){
            ApplicationUser user = response.get();
            boolean added = user.addUserRole(Role.ROLE_ADMIN);
            if (added){
                applicationUserService.update(user);
                return new ResponseEntity<>(new Response("USER UPDATED TO ADMIN"), HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(new Response("ALREADY AN ADMIN"), HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>(new Response("USER NOT FOUND"), HttpStatus.NOT_FOUND);
    }
    public ApplicationUser buildUser(ApplicationUserRequest applicationUser) {
        return ApplicationUser.builder()
                .id(applicationUser.getId())
                .firstname(applicationUser.getFirstname())
                .lastname(applicationUser.getLastname())
                .middleName(applicationUser.getMiddleName())
                .password(passwordEncoder.encode(applicationUser.getLastname()).toUpperCase())
                .schoolEmail(applicationUser.getSchoolEmail())
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isAccountNonExpired(true)
                .isEnabled(true)
                .build();
    }
}
