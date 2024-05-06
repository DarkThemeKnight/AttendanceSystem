package com.example.clientSide.service;

import com.example.clientSide.state.ApplicationContext;
import com.example.clientSide.utils.AuthenticationRequest;
import com.example.clientSide.utils.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
@Slf4j
public class AuthenticationService {
    public static ObjectMapper objectMapper  = new ObjectMapper();
    public static String login(AuthenticationRequest request) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Define login URL
        String loginUrl = "http://localhost:8080/api/v1/auth/login";
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create HttpEntity with request body and headers
        HttpEntity<AuthenticationRequest> httpEntity = new HttpEntity<>(request, headers);
        try {
            // Send POST request
            ResponseEntity<AuthenticationResponse> responseEntity = restTemplate.exchange(
                    loginUrl,
                    HttpMethod.POST,
                    httpEntity,
                    AuthenticationResponse.class);
            // Handle response
            handleLoginResponse(responseEntity);
            return "SUCCESS";
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                log.warn("User not found!");
                return "Invalid Username or password";
            } else if (statusCode == HttpStatus.LOCKED) {
                log.warn("Account locked!");
                return "Your account has been locked contact admin";
            } else {
                log.warn("Unexpected client error: " + statusCode);
                return "Error occurred";
            }
        } catch (Exception ex) {
            // Handle other exceptions
           log.error("Unexpected error: " + ex.getMessage());
           return "Error occurred";
        }
    }
    private static void handleLoginResponse(ResponseEntity<AuthenticationResponse> responseEntity) {
        ApplicationContext context = ApplicationContext.getInstance();
        context.setJwtToken(Objects.requireNonNull(responseEntity.getBody()).getJwtToken());
        context.setLoggedInUser(General.getUser(context.getJwtToken()));
        log.info("Login configuration Is Successful");
    }
}
