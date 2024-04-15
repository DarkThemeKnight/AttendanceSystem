package com.backend.FaceRecognition.utils;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
            ApplicationUser user = new ApplicationUser("0001", "Omotola",
                    "David", "Ayanfeoluwa",
                    "ayanfeoluwadafidi@outlook.com",passwordEncoder.encode("141066"), Collections.singleton(Role.ROLE_SUPER_ADMIN), true, true, true, true);
            userService.create(user);
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
