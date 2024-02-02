package com.unilorin.attendance_system.authentication_api.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
@EnableWebSecurity
@EnableMethodSecurity
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
                    .authorizeHttpRequests(requests -> requests
                            .requestMatchers("/auth/v1/register","/auth/v1/login")
                            .permitAll()
                            .requestMatchers("/auth/v1/register/admin")
                            .hasAuthority("ROLE_SUPER_ADMIN")
                            .requestMatchers("/auth/v1/logout")
                            .authenticated()
                            .anyRequest()
                            .denyAll()
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