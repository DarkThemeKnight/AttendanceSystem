package com.unilorin.attendance_system.datapersistence_api.service;

import com.unilorin.attendance_system.datapersistence_api.constants.Role;
import com.unilorin.attendance_system.datapersistence_api.entity.ApplicationUser;
import com.unilorin.attendance_system.datapersistence_api.entity.Subject;
import com.unilorin.attendance_system.datapersistence_api.repo.ApplicationUserRepository;
import com.unilorin.attendance_system.datapersistence_api.repo.SubjectRepository;
import com.unilorin.attendance_system.datapersistence_api.utils.ApplicationUserRequest;
import com.unilorin.attendance_system.datapersistence_api.utils.ApplicationUserResponse;
import com.unilorin.attendance_system.datapersistence_api.utils.SubjectResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplicationUserService{
    private final ApplicationUserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Autowired
    public ApplicationUserService(ApplicationUserRepository userRepository, SubjectRepository subjectRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ResponseEntity<ApplicationUserResponse> findUser(String userId) {
        log.info("Finding user with ID: {}", userId);

        Optional<ApplicationUser> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.warn("User with ID {} not found", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ApplicationUser user = userOptional.get();
        if (!user.isEnabled()) {
            log.warn("User with ID {} is not enabled", userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        ApplicationUserResponse applicationUserResponse = parse(user);
        log.info("User with ID {} found successfully", userId);
        return new ResponseEntity<>(applicationUserResponse, HttpStatus.OK);
    }

    private ApplicationUserResponse parse(ApplicationUser user){
        ApplicationUserResponse applicationUserResponse = new ApplicationUserResponse();
        applicationUserResponse.setId(user.getId());
        applicationUserResponse.setUserRole(user.getUserRole());
        applicationUserResponse.setFirstname(user.getFirstname());
        applicationUserResponse.setLastname(user.getLastname());
        applicationUserResponse.setMiddleName(user.getMiddleName());
        applicationUserResponse.setUserRole(user.getUserRole());
        applicationUserResponse.setPassword(user.getPassword());
        applicationUserResponse.setSchoolEmail(user.getSchoolEmail());
        Set<Subject> subjects = subjectRepository.findAllByLecturerInCharge(user);
        Set<SubjectResponseDto> subjectDto = subjects.stream().map(subject -> new SubjectResponseDto(subject.getSubjectCode(),subject.getSubjectTitle())).collect(Collectors.toSet());
        applicationUserResponse.setSubjects(subjectDto);
        applicationUserResponse.setAccountNonExpired(user.isAccountNonExpired());
        applicationUserResponse.setEnabled(user.isEnabled());
        applicationUserResponse.setCredentialsNonExpired(user.isCredentialsNonExpired());
        applicationUserResponse.setAccountNonLocked(user.isAccountNonLocked());
        return applicationUserResponse;
    }
    public ResponseEntity<ApplicationUserResponse> login(String id, String password) {
        log.info("Attempting login for user with ID: {}", id);

        Optional<ApplicationUser> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Unauthorized login attempt for user with ID: {}", id);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            user.setCredentialsNonExpired(true);
            user = userRepository.save(user);
            log.info("User with ID {} logged in successfully", id);
            return new ResponseEntity<>(parse(user), HttpStatus.OK);
        }
        log.warn("User with ID {} not found", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    public ResponseEntity<ApplicationUserResponse> create(ApplicationUserRequest appUser) {
        log.info("Creating user with ID: {}", appUser.getId());

        Optional<ApplicationUser> userOptional = userRepository.findById(appUser.getId());
        if (userOptional.isEmpty()) {
            ApplicationUser applicationUser = ApplicationUser.builder()
                    .id(appUser.getId())
                    .userRole(appUser.getUserRole())
                    .firstname(appUser.getFirstname())
                    .lastname(appUser.getLastname())
                    .middleName(appUser.getMiddleName())
                    .password(passwordEncoder.encode(appUser.getPassword()))
                    .schoolEmail(appUser.getSchoolEmail())
                    .userRole(appUser.getUserRole())
                    .isEnabled(true)
                    .isAccountNonExpired(true)
                    .isCredentialsNonExpired(true)
                    .isAccountNonLocked(true)
                    .build();
            applicationUser = userRepository.save(applicationUser);
            log.info("User with ID {} created successfully", appUser.getId());
            return new ResponseEntity<>(parse(applicationUser), HttpStatus.OK);
        }

        log.warn("User with ID {} already exists", appUser.getId());
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    public ResponseEntity<String> update(String bearerToken, String updateType, String update) {
        String jwt_token = jwtService.extractTokenFromHeader(bearerToken);
        String id = jwtService.getId(jwt_token);
        log.info("Updating user with ID: {}", id);
        Optional<ApplicationUser> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            log.warn("User with ID {} not found", id);
            return new ResponseEntity<>("user not found", HttpStatus.NOT_FOUND);
        }
        switch (updateType) {
            case "password" -> {
                log.info("Updating password for user with ID: {}", id);
                ApplicationUser user = userOptional.get();
                user.setPassword(update);
                userRepository.save(user);
                return new ResponseEntity<>("updated Successfully", HttpStatus.OK);
            }
            case "remove_subject" -> {
                log.info("Removing subject for user with ID: {}", id);
                ApplicationUser user = userOptional.get();
                Subject subject = subjectRepository.findById(update).orElse(null);
                if (subject == null) {
                    return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
                }
                ApplicationUser applicationUser = subject.getLecturerInCharge();
                if (!applicationUser.getId().equals(id)) {
                    return new ResponseEntity<>("Unauthorized", HttpStatus.BAD_REQUEST);
                }
                subject.setLecturerInCharge(null);
                subjectRepository.save(subject);
                userRepository.save(user);
                return new ResponseEntity<>("updated successfully", HttpStatus.OK);
            }
            case "credentials" -> {
                log.info("Updating credential status");
                ApplicationUser user = userOptional.get();
                switch (update) {
                    case "true" -> user.setCredentialsNonExpired(true);
                    case "false" -> user.setCredentialsNonExpired(false);
                    default -> {
                        log.error("Bad credential {}",update);
                        return new ResponseEntity<>(update + " is not valid request", HttpStatus.BAD_REQUEST);
                    }
                }
                userRepository.save(user);
                return new ResponseEntity<>("User Credentials adjusted", HttpStatus.OK);
            }
            case "enable" -> {
                String  mail = jwtService.getId(jwtService.extractTokenFromHeader(bearerToken));
                Optional<ApplicationUser> applicationUser = userRepository.findById(mail);
                if (applicationUser.isEmpty()){
                    return new ResponseEntity<>("Forbidden request",HttpStatus.FORBIDDEN);
                }
                ApplicationUser admin  = applicationUser.get();
                if (admin.getUserRole().stream().filter(role -> role == Role.ROLE_ADMIN || role == Role.ROLE_SUPER_ADMIN).toList().isEmpty()){
                    return new ResponseEntity<>("Forbidden request",HttpStatus.FORBIDDEN);
                }
                ApplicationUser user = userOptional.get();
                if (user.getUserRole().contains(Role.ROLE_ADMIN) && !user.getUserRole().contains(Role.ROLE_SUPER_ADMIN)){
                    return new ResponseEntity<>("Forbidden request",HttpStatus.FORBIDDEN);
                }
                switch (update) {
                    case "true" -> {
                        user.setEnabled(true);
                        userRepository.save(user);
                        return new ResponseEntity<>("Enabled account", HttpStatus.OK);
                    }
                    case "false" -> {
                        user.setEnabled(false);
                        userRepository.save(user);
                        return new ResponseEntity<>("Disabled account", HttpStatus.OK);
                    }
                    default -> {
                        log.warn("Invalid update: {}", update);
                        return new ResponseEntity<>("Invalid update value",HttpStatus.BAD_REQUEST);
                    }
                }
            }
            default -> {
                log.warn("Invalid update type: {}", updateType);
                return new ResponseEntity<>("invalid update request", HttpStatus.BAD_REQUEST);
            }
        }
    }
}
