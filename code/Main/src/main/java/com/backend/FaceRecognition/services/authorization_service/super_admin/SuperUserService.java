package com.backend.FaceRecognition.services.authorization_service.super_admin;
import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
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
    public SuperUserService(ApplicationUserService applicationUserService, PasswordEncoder passwordEncoder) {
        this.applicationUserService = applicationUserService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Adds a new admin user.
     * This method creates a new admin user based on the provided request.
     * The user's role is set to ROLE_ADMIN,
     * and the user is then created using the application user service.
     * If the user creation is successful,
     * an OK response is returned indicating that the admin user has been added.
     * If the user already exists,
     * a conflict response is returned.
     *
     * @param request The request containing the details of the admin user to be added.
     * @return A ResponseEntity indicating the result of the operation.
     *         If the admin user is successfully added, an OK response is returned.
     *         If the user already exists, a conflict response is returned.
     */
    public ResponseEntity<Response> addNewAdmin(ApplicationUserRequest request){
        ApplicationUser user = buildUser(request);
        user.setUserRole(Set.of(Role.ROLE_ADMIN));
        ResponseEntity<Void> response= applicationUserService.create(user);
        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(new Response("Admin added"), HttpStatus.OK);
    }

    /**
     * Sets a user to the admin role.
     * This method attempts to set the user with the provided ID to the admin role. If the user is found and
     * successfully set to the admin role, an OK response is returned. If the user is already an admin,
     * a conflict response is returned. If the user is not found, a not found response is returned.
     *
     * @param id The ID of the user to be set as admin.
     * @return A ResponseEntity indicating the result of the operation.
     *         If the user is successfully updated to admin, an OK response is returned.
     *         If the user is already an admin, a conflict response is returned.
     *         If the user is not found, a not found response is returned.
     */
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
    /**
     * Locks the user account.
     * This method locks the user account associated with the provided ID. If the user is found and successfully
     * locked, an OK response is returned with a message indicating that the account has been locked successfully.
     * If the user is not found, a not found response is returned. If the user is a super admin, an unauthorized
     * response is returned indicating that the account cannot be locked.
     *
     * @param id The ID of the user account to be locked.
     * @return A ResponseEntity indicating the result of the operation.
     *         If the account is successfully locked, an OK response is returned.
     *         If the user is not found, a not found response is returned.
     *         If the user is a super admin, an unauthorized response is returned.
     */
    public ResponseEntity<Response> lockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized to lock account."));
            }
            user.setEnabled(false);
            applicationUserService.update(user);
            return ResponseEntity.ok(new Response("Account locked successfully."));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Unlocks the user account.
     * This method unlocks the user account associated with the provided ID. If the user is found and successfully
     * unlocked,
     * an OK response is returned with a message indicating that the account has been unlocked successfully.
     * If the user is not found, a not found response is returned.
     * If the user is a super admin, an unauthorized
     * response is returned indicating that the account cannot be unlocked.
     *
     * @param id The ID of the user account to be unlocked.
     * @return A ResponseEntity indicating the result of the operation.
     *         If the account is successfully unlocked, an OK response is returned.
     *         If the user is not found, a not found response is returned.
     *         If the user is a super admin, an unauthorized response is returned.
     */
    public ResponseEntity<Response> unlockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Unauthorized to unlock account."));
            }
            user.setEnabled(true);  // Corrected to set the account as enabled
            applicationUserService.update(user);
            return ResponseEntity.ok(new Response("Account unlocked successfully."));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
