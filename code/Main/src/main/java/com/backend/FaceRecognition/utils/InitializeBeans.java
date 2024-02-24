package com.backend.FaceRecognition.utils;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class InitializeBeans {
    private final ApplicationUserService userService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public InitializeBeans(ApplicationUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    @Bean
    public CommandLineRunner setupSuperUser(){
        return args -> {
            log.info("Setting up super user");
            ApplicationUser user = new ApplicationUser("0001", "Omotola", "David", "Ayanfeoluwa", "18-30gr053@students.unilorin.edu.ng",passwordEncoder.encode("DaviDOMOTOLA"), Collections.singleton(Role.ROLE_SUPER_ADMIN), true, true, true, true);
            userService.create(user);
            log.info("Successfully setup application owner");
        };
    }
}
