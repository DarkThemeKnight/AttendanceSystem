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
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String userId;
        if (authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        try {
            token = authHeader.substring(7);
            userId = jwtService.getId(token);
            if(!userId.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null){
                ApplicationUser applicationUser = userRepository.findById(userId).orElse(null);
                if(applicationUser == null){
                    response.sendError(HttpStatus.NOT_FOUND.value(),"Username not found");
                    return;
                }

                if (!applicationUser.isEnabled()){
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
