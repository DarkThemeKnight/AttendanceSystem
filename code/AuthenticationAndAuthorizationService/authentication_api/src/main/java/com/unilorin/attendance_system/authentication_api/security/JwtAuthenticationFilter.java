package com.unilorin.attendance_system.authentication_api.security;
import com.unilorin.attendance_system.authentication_api.entity.ApplicationUser;
import com.unilorin.attendance_system.authentication_api.repo.ApplicationUserRepository;
import com.unilorin.attendance_system.authentication_api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ApplicationUserRepository userRepository;
    @Autowired
    public JwtAuthenticationFilter(@Lazy ApplicationUserRepository userService, JwtService service){
        this.userRepository = userService;
        jwtService = service;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String userMail;
        if (authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        token = authHeader.substring(7);
        userMail = jwtService.extractMail(token);
        if(!userMail.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null){
            Optional<ApplicationUser> applicationUser = userRepository.findById(userMail);
            if(applicationUser.isEmpty()){
                throw new UsernameNotFoundException("Username not found");
            }
            if (jwtService.isValidToken(token, applicationUser.get())){
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userRepository, null, applicationUser.get().getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authenticationToken);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(request,response);
    }

}