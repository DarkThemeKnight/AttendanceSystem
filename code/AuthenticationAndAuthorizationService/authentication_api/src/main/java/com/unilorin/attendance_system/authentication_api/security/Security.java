package com.unilorin.attendance_system.authentication_api.security;

import com.unilorin.attendance_system.authentication_api.entity.ApplicationUser;
import com.unilorin.attendance_system.authentication_api.repo.ApplicationUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@Slf4j
public class Security {
    private final ApplicationUserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    public Security(ApplicationUserRepository userRepository, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    @Bean
    public SecurityFilterChain  securityFilterChain(HttpSecurity security) {
        DefaultSecurityFilterChain filterChain = null;
        try {
            filterChain = security.csrf(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(managementConfigure -> managementConfigure.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                            authorizationManagerRequestMatcherRegistry
                                    .anyRequest()
                                    .permitAll()
                    )
                    .authenticationManager(authManager())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }catch (Exception e) {
            log.error("Exception in filter Security filter chain");
            throw new RuntimeException(e);
        }
        return filterChain;
    }
    @Bean
    public AuthenticationManager authManager(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(mail -> {
            Optional<ApplicationUser> applicationUser = userRepository.findById(mail);
            if (applicationUser.isEmpty()){
                throw new UsernameNotFoundException("User "+mail+" is not found");
            }
            return applicationUser.get();
        });
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
