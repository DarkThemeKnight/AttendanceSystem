package com.backend.FaceRecognition.services.authorization_service.super_admin;
import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserDataPersistenceService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;

@Service
public class SuperUserService {
    private final ApplicationUserDataPersistenceService applicationUserDataPersistenceService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public SuperUserService(ApplicationUserDataPersistenceService applicationUserDataPersistenceService, PasswordEncoder passwordEncoder) {
        this.applicationUserDataPersistenceService = applicationUserDataPersistenceService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<String> addNewAdmin(ApplicationUserRequest request){
        ApplicationUser user = buildUser(request);
        user.setUserRole(Set.of(Role.ROLE_ADMIN));
        ResponseEntity<Void> response= applicationUserDataPersistenceService.create(user);
        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return new ResponseEntity<>("User already Exists", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>("Admin added",HttpStatus.OK);
    }
    public ResponseEntity<String> setToAdmin(String id){
        Optional<ApplicationUser> response= applicationUserDataPersistenceService.findUser(id);
        if (response.isPresent()){
            ApplicationUser user = response.get();
            boolean added = user.addUserRole(Role.ROLE_ADMIN);
            if (added){
                applicationUserDataPersistenceService.update(user);
                return new ResponseEntity<>("USER UPDATED TO ADMIN",HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("ALREADY AN ADMIN",HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>("USER NOT FOUND",HttpStatus.NOT_FOUND);
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
    public ResponseEntity<String> lockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserDataPersistenceService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to lock account.");
            }
            user.setEnabled(false);
            applicationUserDataPersistenceService.update(user);
            return ResponseEntity.ok("Account locked successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    public ResponseEntity<String> unlockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserDataPersistenceService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to lock account.");
            }
            user.setEnabled(false);
            applicationUserDataPersistenceService.update(user);
            return ResponseEntity.ok("Account locked successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
