package com.backend.FaceRecognition.helper;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.controller.*;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.utils.FaceRecognitionEndpoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitializeBeans {
    private final ApplicationUserService userService;
    private final PasswordEncoder passwordEncoder;


    private void setupSuperAdmin(){
        ApplicationUser user = new ApplicationUser(
                "0001",
                "Omotola",
                "David",
                "Ayanfeoluwa",
                "ayanfeoluwadafidi@outlook.com",
                passwordEncoder.encode("141066"),
                "Demo Address",
                "08055132800",
                Set.of(Role.ROLE_SUPER_ADMIN),
                true,
                true,
                true,
                true,
                null);
        userService.create(user);
    }
    private void setupAdmin(){
       var user = ApplicationUser.builder()
                .id("0002")
                .address("Demo Address")
                .phoneNumber("029203939202")
                .userRole(Set.of(Role.ROLE_ADMIN))
                .firstname("admin1")
                .lastname("demoSurname")
                .password(passwordEncoder.encode("DEMOSURNAME"))
                .isAccountNonExpired(true)
                .isEnabled(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .build();
        userService.create(user);
    }
    private void setupLecturer(){
        var user = ApplicationUser.builder()
                .id("i0001")
                .address("Demo Address")
                .phoneNumber("029203939202")
                .userRole(Set.of(Role.ROLE_LECTURER))
                .firstname("demo_lecturer1")
                .lastname("demoSurname")
                .password(passwordEncoder.encode("DEMOSURNAME"))
                .isAccountNonExpired(true)
                .isEnabled(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .build();
        userService.create(user);
    }

    @Bean
    public CommandLineRunner setupApplication(){
        return args -> {
            setupAdmin();
            setupLecturer();
            setupSuperAdmin();


        };
    }
    @Bean
    public FaceRecognitionEndpoints initializeEndpoints(){
        Map<String,String> endpointMap = new HashMap<>();
        endpointMap.put("ip","http://localhost:8000/api/v1/image-processing");
        endpointMap.put("rec","http://localhost:8000/api/v1/recognize");
        return new FaceRecognitionEndpoints(endpointMap);
    }
}
