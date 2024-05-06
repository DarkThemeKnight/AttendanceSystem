package com.example.clientSide.service;

import com.example.clientSide.utils.ApplicationUser;
import com.example.clientSide.utils.ApplicationUserRequest;
import com.example.clientSide.utils.GetListOfUsers;
import com.example.clientSide.utils.Response;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class SuperAdminService {
    public void getAllUsers(String userType, String bearerToken) {
        String SUPER_ADMIN_URL = "http://localhost:8080/api/v1/super-admin/{userType}";
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        // Define URL with path variable
        String url = SUPER_ADMIN_URL.replace("{userType}", userType);

        try {
            // Send GET request
            ResponseEntity<GetListOfUsers> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    GetListOfUsers.class);


        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private static final String ADD_NEW_ADMIN_URL = "http://localhost:8080/api/v1/super-admin/add-new-admin";
    public void addNewAdmin(ApplicationUserRequest request, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create HttpEntity with request body and headers
        HttpEntity<ApplicationUserRequest> httpEntity = new HttpEntity<>(request, headers);
        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    ADD_NEW_ADMIN_URL,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);
            // Handle response

        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.CONFLICT) {
                System.out.println("User is already an admin.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private static final String SET_TO_ADMIN_URL = "http://localhost:8080/api/v1/super-admin/set-to-admin/{userId}";

    public void setUserAsAdmin(String userId, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        // Define URL with path variable
        String url = SET_TO_ADMIN_URL.replace("{userId}", userId);

        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);

            // Handle response
            handleSetToAdminResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                System.out.println("User not found.");
            } else if (statusCode == HttpStatus.CONFLICT) {
                System.out.println("User is already an admin.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleSetToAdminResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("User set to admin successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }
    private static final String GET_USER_URL = "http://localhost:8080/api/v1/super-admin/get-user";

    public void getUser(String userId, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Set query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GET_USER_URL)
                .queryParam("id", userId);
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        try {
            // Send GET request
            ResponseEntity<ApplicationUser> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    ApplicationUser.class);
            // Handle response
            handleGetUserResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                System.out.println("User not found.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleGetUserResponse(ResponseEntity<ApplicationUser> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            ApplicationUser user = responseEntity.getBody();
            // Handle user data
            System.out.println("User: " + user);
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }



}
