package com.unilorin.attendance_system.authentication_api.security;

import com.unilorin.attendance_system.authentication_api.repository_requests.ApplicationUserRequestService;
import com.unilorin.attendance_system.authentication_api.utils.ApplicationUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityBeans {
    private final ApplicationUserRequestService userRepository;
    @Autowired
    public SecurityBeans(ApplicationUserRequestService userRepository) {
        this.userRepository = userRepository;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        log.info("password encoder setup");
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authManager(){
        log.info("auth manager setup");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(id -> {
            ApplicationUser applicationUser = userRepository.findUser(id);
            if (applicationUser==null){
                throw new UsernameNotFoundException("User "+id+" is not found");
            }
            return applicationUser;
        });
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

}