package com.example.clientSide.service;
import com.example.clientSide.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
@Slf4j
public class AdminServiceService {
    private static final String SETUP_NOTIFICATIONS_URL = "http://localhost:8080/api/v1/admin/setup-notifications";
    public void setupNotifications(NotificationRequest request, String bearerToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NotificationRequest> httpEntity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    SETUP_NOTIFICATIONS_URL,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);
            handleSetupNotificationsResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.BAD_REQUEST) {
                System.out.println("Bad request.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleSetupNotificationsResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Notifications set up successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }
    private static final String GET_ALL_SUBJECTS_URL = "http://localhost:8080/api/v1/admin/subject";
    public void getAllSubjects(boolean isStudent, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Set query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GET_ALL_SUBJECTS_URL)
                .queryParam("student", isStudent);
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        try {
            // Send GET request
            ResponseEntity<AllSubjects> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    AllSubjects.class);
            // Handle response
            handleGetAllSubjectsResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                System.out.println("Subjects not found.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private void handleGetAllSubjectsResponse(ResponseEntity<AllSubjects> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            AllSubjects subjects = responseEntity.getBody();
            System.out.println("Subjects: " + subjects);
        } else {
            System.out.println("Unexpected response: " + statusCode);
        }
    }


    private static final String UPDATE_SUBJECT_URL = "http://localhost:8080/api/v1/admin/update-subject";

    public void updateSubject(SubjectRequest request, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with request body and headers
        HttpEntity<SubjectRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            // Send PUT request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    UPDATE_SUBJECT_URL,
                    HttpMethod.PUT,
                    httpEntity,
                    Response.class);

            // Handle response
            handleUpdateSubjectResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.BAD_REQUEST) {
                System.out.println("Bad request.");
            } else if (statusCode == HttpStatus.CONFLICT) {
                System.out.println("Conflict: Subject already exists or another conflict.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleUpdateSubjectResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Subject updated successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private static final String ADD_USER_URL = "http://localhost:8080/api/v1/admin/register";

    public void addUser(ApplicationUserRequest request, String userType, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ApplicationUserRequest> httpEntity = new HttpEntity<>(request, headers);

        String url = ADD_USER_URL + "?type=" + userType.toLowerCase();

        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);

            // Handle response
            handleAddUserResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.BAD_REQUEST) {
                System.out.println("Bad request.");
            } else if (statusCode == HttpStatus.CONFLICT) {
                System.out.println("Conflict: User already exists.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleAddUserResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("User added successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }
    private static final String LOCK_ACCOUNT_URL = "http://localhost:8080/api/v1/admin/lock-account";
    public void lockAccount(String userId, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        // Define URL with query parameter for user id
        String url = LOCK_ACCOUNT_URL + "?id=" + userId;
        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);
            // Handle response
            handleLockAccountResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private void handleLockAccountResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Account locked successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private static final String UNLOCK_ACCOUNT_URL = "http://localhost:8080/api/v1/admin/unlock-account";

    public void unlockAccount(String userId, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        // Define URL with query parameter for user id
        String url = UNLOCK_ACCOUNT_URL + "?id=" + userId;

        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);

            // Handle response
            handleUnlockAccountResponse(responseEntity);
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

    private void handleUnlockAccountResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Account unlocked successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private static final String ADD_SUBJECT_URL = "http://localhost:8080/api/v1/admin/add-subject";
    public void addSubject(SubjectRequest request, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with request body and headers
        HttpEntity<SubjectRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    ADD_SUBJECT_URL,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);

            // Handle response
            handleAddSubjectResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.BAD_REQUEST) {
                System.out.println("Bad request.");
            } else if (statusCode == HttpStatus.CONFLICT) {
                System.out.println("Conflict: Subject already exists or another conflict.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleAddSubjectResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Subject added successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private static final String GET_SUBJECT_URL = "http://localhost:8080/api/v1/admin/subject/{code}";
    public void getSubject(String code, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        // Define URL with path variable for subject code
        String url = GET_SUBJECT_URL.replace("{code}", code);

        try {
            // Send GET request
            ResponseEntity<AllSubjects> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    AllSubjects.class);

            // Handle response
            handleGetSubjectResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                System.out.println("Subject not found.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleGetSubjectResponse(ResponseEntity<AllSubjects> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            AllSubjects subjects = responseEntity.getBody();
            // Handle list of subjects
            System.out.println("Subjects: " + subjects);
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private static final String DELETE_SUBJECT_URL = "http://localhost:8080/api/v1/admin/subject";

    public void deleteSubject(String code, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        // Define URL with query parameter for subject code
        String url = DELETE_SUBJECT_URL + "?id=" + code;

        try {
            // Send DELETE request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    httpEntity,
                    Response.class);

            // Handle response
            handleDeleteSubjectResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                System.out.println("Subject not found.");
            } else {
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            }
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleDeleteSubjectResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Subject deleted successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }


}