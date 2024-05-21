package com.backend.FaceRecognition.utils;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InitializeBeans {
    private final ApplicationUserService userService;
    private final PasswordEncoder passwordEncoder;

    public InitializeBeans(ApplicationUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    @Bean
    public CommandLineRunner setupSuperUser(){
        return args -> {
            log.info("Setting up super user");
            List<ApplicationUser> users = new ArrayList<>();
            ApplicationUser user = new ApplicationUser("0001", "Omotola",
                    "David",
                    "Ayanfeoluwa",
                    "ayanfeoluwadafidi@outlook.com",
                    passwordEncoder.encode("141066"),
//                    LocalDate.of(2008,3,12),
                    "Demo Address",
                    "08055132800",
                    Set.of(Role.ROLE_SUPER_ADMIN),
                    true,
                    true,
                    true,
                    true,
                    null);
            users.add(user);
            user = ApplicationUser.builder()
                    .id("0002")
                    .address("Demo Address")
                    .phoneNumber("029203939202")
                    .userRole(Set.of(Role.ROLE_ADMIN))
//                    .dateOfBirth(LocalDate.of(2001,01,01))
                    .firstname("admin1")
                    .lastname("demoSurname")
                    .password(passwordEncoder.encode("DEMOSURNAME"))
                    .isAccountNonExpired(true)
                    .isEnabled(true)
                    .isCredentialsNonExpired(true)
                    .isAccountNonLocked(true)
                    .build();
            users.add(user);


            for (ApplicationUser userx: users) {
                userService.create(userx);
            }
            log.info("Successfully setup application owner");
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
