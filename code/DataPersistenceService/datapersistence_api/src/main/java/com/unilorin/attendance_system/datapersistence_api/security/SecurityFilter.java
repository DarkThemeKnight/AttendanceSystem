package com.unilorin.attendance_system.datapersistence_api.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Slf4j
@Configuration
public class SecurityFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtAuthFilter jwtAuthFilter;
    @Autowired
    public SecurityFilter(AuthenticationManager authenticationManager, JwtAuthFilter jwtAuthFilter) {
        this.authenticationManager = authenticationManager;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public DefaultSecurityFilterChain securityFilterChain(HttpSecurity security) {
        DefaultSecurityFilterChain filterChain = null;
        try {
            filterChain = security.csrf(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(managementConfigure -> managementConfigure.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                            authorizationManagerRequestMatcherRegistry
                                    .requestMatchers("api/v1/user/get/{id}","api/v1/user/create","api/v1/user/login")
                                    .permitAll()
                                    .anyRequest()
                                    .authenticated()
                    )
                    .authenticationManager(authenticationManager)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(
                            httpSecurityExceptionHandlingConfigurer ->
                                    httpSecurityExceptionHandlingConfigurer.
                                            authenticationEntryPoint(
                                                    (request, response, authException) ->{
                                                        log.error("Unauthorized error: {}  cant access resource", authException.getMessage());
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                                    }
                                            )
                                            .accessDeniedHandler(
                                                    (request, response, accessDeniedException) ->{
                                                        log.error("Access denied error: {}", accessDeniedException.getMessage());
                                                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                                                    }
                                            )
                    )
                    .build();
        }catch (Exception e) {
            log.error("Exception in filter Security filter chain");
            throw new RuntimeException(e);
        }
        return filterChain;
    }

}
