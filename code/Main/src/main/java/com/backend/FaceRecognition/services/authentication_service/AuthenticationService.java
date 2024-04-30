package com.backend.FaceRecognition.services.authentication_service;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.ResetPasswordToken;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.repository.ResetPasswordTokenSaltRepository;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.mail.MailService;
import com.backend.FaceRecognition.utils.ResetPassword;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AuthenticationService {
    private final ApplicationUserService applicationUserService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;
    private final MailService mailService;
    private final ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository;

    public AuthenticationService(ApplicationUserService applicationUserService, JwtService jwtService,
                                 PasswordEncoder passwordEncoder, StudentService studentService, MailService mailService, ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository) {
        this.applicationUserService = applicationUserService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
        this.mailService = mailService;
        this.resetPasswordTokenSaltRepository = resetPasswordTokenSaltRepository;
    }

    public ResponseEntity<Response> register(ApplicationUserRequest applicationUser, String type) {
        ApplicationUser user = buildUser(applicationUser);
        return switch (type) {
            case "instructor" -> {
                user.setUserRole(Set.of(Role.ROLE_LECTURER));
                ResponseEntity<Void> response = applicationUserService.create(user);
                if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    yield new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
                }
                yield new ResponseEntity<>(new Response("User Registered Successfully"), HttpStatus.OK);
            }
            case "student" -> {
                user.setUserRole(Set.of(Role.ROLE_STUDENT));
                ResponseEntity<Void> response = applicationUserService.create(user);
                if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    yield new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
                }
                Student student = buildStudent(user);
                studentService.saveStudent(student);
                yield ResponseEntity.ok(new Response("Student Added Successfully"));
            }
            default -> ResponseEntity.badRequest().body(new Response("Bad Type"));
        };
    }

    private String defaultPassword(String lastname) {
        return passwordEncoder.encode(lastname.toUpperCase());
    }

    public Student buildStudent(ApplicationUser request) {
        Student student = new Student();
        student.setFirstname(request.getFirstname());
        student.setLastname(request.getLastname());
        student.setMatriculationNumber(request.getId());
        student.setMiddleName(request.getMiddleName());
        student.setSchoolEmail(request.getSchoolEmail());
        return student;
    }

    private ApplicationUser buildUser(ApplicationUserRequest applicationUser) {
        return ApplicationUser.builder()
                .id(applicationUser.getId())
                .firstname(applicationUser.getFirstname())
                .lastname(applicationUser.getLastname())
                .middleName(applicationUser.getMiddleName())
                .password(defaultPassword(applicationUser.getLastname()))
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
     * It first retrieves the user based on the provided ID from the application
     * user service.
     * If the user is found, it checks if the account is enabled. If the account is
     * enabled
     * and the provided password matches the user's password, it updates the user's
     * credentials
     * expiration status, generates a JWT token for the user, and returns a success
     * response with
     * the generated token. If the account is locked or the provided password is
     * incorrect,
     * appropriate error responses are returned.
     *
     * @param request The authentication request containing the user ID and
     *                password.
     * @return A ResponseEntity containing an AuthenticationResponse object.
     *         If the login is successful, a JWT token is returned along with a
     *         success response (200).
     *         If the account is locked, a locked account response (423) is
     *         returned.
     *         If the user is not found or the password is incorrect, a not found
     *         response (404)
     *         or a conflict response (409) is returned, respectively.
     */
    public ResponseEntity<AuthenticationResponse> login(AuthenticationRequest request) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(request.getId());
        if (userOptional.isEmpty()){
            log.info("Cannot find user");
        }
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            log.info("User found => {}",user.getId());
            if (!user.isEnabled()) {
                log.warn("Locked account of id => {} is trying to access", request.getId());
                return new ResponseEntity<>(new AuthenticationResponse("Locked Account", null, new HashSet<>()),
                        HttpStatus.LOCKED);
            }
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                user.setCredentialsNonExpired(true);
                applicationUserService.update(user);
                String token = jwtService.generate(new HashMap<>(), user);
                log.info(("Successful login {}"), request.getId());
                return new ResponseEntity<>(new AuthenticationResponse("Login successfully", token, user.getUserRole()),
                        HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new AuthenticationResponse("Invalid Username or Password", null, new HashSet<>()),
                HttpStatus.NOT_FOUND);
    }

    /**
     * Logs out a user based on the provided JWT token.
     * This method logs out a user based on the provided JWT token.
     * It first extracts the user ID from
     * the JWT token.
     * Then, it retrieves the user based on the extracted ID from the application
     * user service.
     * If the user is found, it sets the user's credential expiration status too
     * false to invalidate the token
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
    public ResponseEntity<Void> logout(String bearerToken) {
        String id = jwtService.getId(jwtService.extractTokenFromHeader(bearerToken));
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            user.setCredentialsNonExpired(false);
            applicationUserService.update(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Response> forgotPassword(String id) {
       Response response = mailService.sendForgotPasswordResetLink(id);
       if (response.getMessage().equalsIgnoreCase(HttpStatus.NOT_FOUND.name())){
           return new ResponseEntity<>(new Response("User not found"),HttpStatus.NOT_FOUND);
       }
       return ResponseEntity.ok(new Response("Email Sent Successfully"));
    }

    public ResponseEntity<Response> resetPassword(String token, ResetPassword resetPassword) {
        Optional<ResetPasswordToken> resetPasswordTokenOptional = resetPasswordTokenSaltRepository.findBySalt(token);
        if (resetPasswordTokenOptional.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        ResetPasswordToken val = resetPasswordTokenOptional.get();
        if (LocalDateTime.now().isAfter(val.getExpiryDateTime())){
            return  ResponseEntity.badRequest().body(new Response("Link Expired"));
        }
        return applicationUserService.resetPassword(val.getUserId(),resetPassword);
    }


}