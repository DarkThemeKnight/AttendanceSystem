package com.unilorin.attendance_system.authentication_api.security;

import com.unilorin.attendance_system.authentication_api.repository_requests.ApplicationUserRequestService;
import com.unilorin.attendance_system.authentication_api.service.token_management.JwtService;
import com.unilorin.attendance_system.authentication_api.utils.ApplicationUser;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ApplicationUserRequestService userRepository;
    @Autowired
    public JwtAuthFilter(JwtService jwtService, @Lazy ApplicationUserRequestService userService ){
        this.jwtService = jwtService;
        this.userRepository = userService;
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
        try {
            token = authHeader.substring(7);
            userMail = jwtService.extractID(token);
            log.info("user id {}",userMail);
            if(!userMail.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null){
                ApplicationUser applicationUser = userRepository.findUser(userMail);
                if(applicationUser == null){
                    log.info("User does not exist");
                    response.sendError(HttpStatus.NOT_FOUND.value(),"Username not found");
                    return;
                }
            log.info("password {}, Authorities => {}",applicationUser.getPassword(),applicationUser.getAuthorities());
            if (!applicationUser.isEnabled()){
                log.info("Disabled account");
                response.sendError(HttpStatus.BAD_REQUEST.value(),"Disabled Account");
                return;
            }
            if (!applicationUser.isCredentialsNonExpired()){
                log.info("Invalid credentials");
                response.sendError(HttpStatus.BAD_REQUEST.value(),"Expired credentials");
                return;
            }
            if (jwtService.isValidToken(token, applicationUser)){
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userRepository, null, applicationUser.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authenticationToken);
                SecurityContextHolder.setContext(context);
            }
        }
        }catch (MalformedJwtException e){
            response.sendError(HttpStatus.BAD_REQUEST.value(),"Malformed Jwt Exception");
        }catch (Exception e){
            response.sendError(HttpStatus.BAD_REQUEST.value(),"Exception occur "+e.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

}