package com.backend.FaceRecognition.security;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.repository.ApplicationUserRepository;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ApplicationUserRepository userRepository;
    public JwtAuthFilter(JwtService jwtService, ApplicationUserRepository userService ){
        this.jwtService = jwtService;
        this.userRepository = userService;
    }
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        log.info("Entry {}",request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String userId;
        if (authHeader==null || !authHeader.startsWith("Bearer ")){
            boolean val = authHeader==null;
            if (val){
                log.info("Null Token");
            }
            else{
                log.info("Token does not start with \"Bearer \"");
            }
            filterChain.doFilter(request,response);
            return;
        }
        try {
            token = authHeader.substring(7);
            userId = jwtService.getId(token);
            if(!userId.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null){
                ApplicationUser applicationUser = userRepository.findById(userId).orElse(null);
                if(applicationUser == null){
                    log.info("User name not found");
                    response.sendError(HttpStatus.UNAUTHORIZED.value(),"Username not found");
                    return;
                }
                if (!applicationUser.isEnabled()){
                    log.info("Disabled");
                    response.sendError(HttpStatus.UNAUTHORIZED.value(),"Disabled Account");
                    return;
                }
                if (!applicationUser.isCredentialsNonExpired()){
                    log.info("Expired Credentials");
                    response.sendError(HttpStatus.UNAUTHORIZED.value(),"Expired credentials");
                    return;
                }
                if (jwtService.isValidToken(token, applicationUser)){
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    log.info("User Id => {}",applicationUser.getId());
                    log.info("User authorities => {}",applicationUser.getAuthorities());
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userRepository, null, applicationUser.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(context);
                }
            }
        }catch (MalformedJwtException e){
            log.info("Malformed Token");
            response.sendError(HttpStatus.BAD_REQUEST.value(),"Malformed Jwt Exception");
        }catch (Exception e){
            log.info("Error");
            response.sendError(HttpStatus.BAD_REQUEST.value(),"Exception occur "+e.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

}
