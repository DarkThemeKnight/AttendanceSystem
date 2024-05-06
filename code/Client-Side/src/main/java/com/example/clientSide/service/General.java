package com.example.clientSide.service;

import com.example.clientSide.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
@Slf4j
public class General {
    public static ApplicationUser getUser(String jwtToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<ApplicationUser> response = restTemplate.exchange(
                    "http://localhost:8080/api/v1/general",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ApplicationUser.class
            );
            log.info("Fetched user");
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.warn("HTTP Client Error: Status => {}", ex.getStatusCode());
            log.warn("Could not fetch user: {}", ex.getResponseBodyAsString());
            return new ApplicationUser();
        } catch (RestClientException ex) {
            log.error("Error fetching user: {}", ex.getMessage());
            return new ApplicationUser();
        }
    }

    private static final String NOTIFICATIONS_URL = "http://localhost:8080/api/v1/general/notification";
    public static NotificationResponse getNotifications(String BEARER_TOKEN) {
        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(BEARER_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        try {
            // Send GET request
            ResponseEntity<NotificationResponse> responseEntity = restTemplate.exchange(
                    NOTIFICATIONS_URL,
                    HttpMethod.GET,
                    httpEntity,
                    NotificationResponse.class);

            // Handle response
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else {
                log.warn("Unexpected response: " + responseEntity.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
    private static final String SCHEDULE_URL = "http://localhost:8080/api/v1/general/schedule";
    public static ResponseEntity<ScheduleSetupResponse> getMySchedule(String BEARER_TOKEN) {
        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(BEARER_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        try {
            // Return response entity
            return restTemplate.exchange(
                    SCHEDULE_URL,
                    HttpMethod.GET,
                    httpEntity,
                    ScheduleSetupResponse.class);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            // Return null if an error occurs
            return null;
        }
    }
    private static final String STUDENT_PROFILE = "http://localhost:8080/api/v1/general/studentProfile";
    public static StudentProfile getProfilePicture(String studentId,String jwtToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<StudentProfile> response = restTemplate.exchange(
                    "http://localhost:8080/api/v1/general/studentProfile?studentId="+studentId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    StudentProfile.class
            );
            log.info("Fetched user");
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.warn("HTTP Client Error: Status => {}", ex.getStatusCode());
            log.warn("Could not fetch user: {}", ex.getResponseBodyAsString());
            return new StudentProfile();
        } catch (RestClientException ex) {
            log.error("Error fetching user: {}", ex.getMessage());
            return new StudentProfile();
        }
    }


    private static final String SCHEDULE_SETUP_URL = "http://localhost:8080/api/v1/general/schedule";
    public static ResponseEntity<ScheduleSetupResponse> addSchedule(ScheduleSetupRequest scheduleRequest,String BEARER_TOKEN) {
        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(BEARER_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create HttpEntity with headers and schedule request body
        HttpEntity<ScheduleSetupRequest> httpEntity = new HttpEntity<>(scheduleRequest, headers);
        try {
            // Return response entity
            return restTemplate.exchange(
                    SCHEDULE_SETUP_URL,
                    HttpMethod.POST,
                    httpEntity,
                    ScheduleSetupResponse.class);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            // Return null if an error occurs
            return null;
        }
    }

}