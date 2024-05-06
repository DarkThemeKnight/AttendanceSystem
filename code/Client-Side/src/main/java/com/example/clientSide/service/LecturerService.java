package com.example.clientSide.service;


import com.example.clientSide.utils.*;
import lombok.NonNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LecturerService {
    private static final String INITIALIZE_ATTENDANCE_URL = "http://localhost:8080/api/v1/attendance/initialize";
    public static void initializeAttendance(String subjectCode, int durationMinutes, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Check if duration is at least 10 minutes
        if (durationMinutes >= 10) {
            // Define URL with query parameters for subject code and duration
            String url = INITIALIZE_ATTENDANCE_URL + "?subjectCode=" + subjectCode + "&duration=" + durationMinutes;
            // Create HttpEntity with headers
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            try {
                // Send GET request
                ResponseEntity<Response> responseEntity = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        httpEntity,
                        Response.class);

                // Handle response
                handleInitializeAttendanceResponse(responseEntity);
            } catch (HttpClientErrorException ex) {
                // Handle HTTP client errors (4xx)
                HttpStatusCode statusCode = ex.getStatusCode();
                System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
            } catch (Exception ex) {
                // Handle other exceptions
                System.out.println("Unexpected error: " + ex.getMessage());
            }
        } else {
            System.out.println("Duration must be at least 10 minutes.");
        }
    }

    private static void handleInitializeAttendanceResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            assert response != null;
            System.out.println("Attendance initialized successfully: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }


    private static final String GET_ATTENDANCE_RECORD_URL = "http://localhost:8080/api/v1/attendance/record";

    public static void getAttendanceRecord(String subjectCode, @NonNull String date, @NonNull String sortId, @NonNull String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build query parameters
        StringBuilder queryParams = new StringBuilder();
        if (subjectCode != null) {
            queryParams.append("subjectCode=").append(subjectCode);
        }
        if (!queryParams.isEmpty()) {
            queryParams.append("&");
        }
        queryParams.append("date=").append(date);
        if (!queryParams.isEmpty()) {
            queryParams.append("&");
        }
        queryParams.append("sort_id=").append(sortId);
        // Define URL with query parameters
        String url = GET_ATTENDANCE_RECORD_URL + "?" + queryParams.toString();
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        try {
            // Send GET request
            ResponseEntity<AttendanceRecordResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    AttendanceRecordResponse.class);
            // Handle response
            handleGetAttendanceRecordResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private static void handleGetAttendanceRecordResponse(ResponseEntity<AttendanceRecordResponse> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            AttendanceRecordResponse attendanceRecord = responseEntity.getBody();
            // Handle successful response
            System.out.println("Attendance record: " + attendanceRecord);
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private static final String GET_AVAILABLE_RECORDS_URL = "http://localhost:8080/api/v1/attendance/available-records";

    public static void getAvailableRecordDates(String subjectCode, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Define URL with query parameter for subject code
        String url = GET_AVAILABLE_RECORDS_URL + "?subjectCode=" + subjectCode;
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        try {
            // Send GET request
            ResponseEntity<AvailableRecords> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    AvailableRecords.class);

            // Handle response
            handleGetAvailableRecordDatesResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private static void handleGetAvailableRecordDatesResponse(ResponseEntity<AvailableRecords> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            AvailableRecords availableRecords = responseEntity.getBody();
            // Handle successful response
            System.out.println("Available record dates: " + availableRecords);
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private static final String CLEAR_STUDENTS_URL = "http://localhost:8080/api/v1/attendance/clear";

    public static void clearStudentsFromClass(String subjectCode, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define URL with query parameter for subject code
        String url = CLEAR_STUDENTS_URL + "?subjectCode=" + subjectCode;

        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);

            // Handle response
            handleClearStudentsResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private static void handleClearStudentsResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Students cleared from class: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }
    private static final String ADD_STUDENT_URL = "http://localhost:8080/api/v1/attendance/add";
    public static void addStudentToClass(String subjectCode, StudentRequest studentRequest, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define URL with query parameter for subject code
        String url = ADD_STUDENT_URL + "?subjectCode=" + subjectCode;

        // Create HttpEntity with request body and headers
        HttpEntity<StudentRequest> httpEntity = new HttpEntity<>(studentRequest, headers);

        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);

            // Handle response
            handleAddStudentResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private static void handleAddStudentResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Student added to class: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }
    private static final String SUSPEND_STUDENT_URL = "http://localhost:8080/api/v1/attendance/suspend";

    public static ResponseEntity<Response> suspendStudentFromClass(String subjectCode, String studentId, boolean suspend, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Define URL with query parameters for subject code, studentId, and suspend status
        String url = SUSPEND_STUDENT_URL + "?subjectCode=" + subjectCode + "&studentId=" + studentId + "&suspend=" + suspend;
        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        try {
            return  restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);
        } catch (HttpClientErrorException ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(ex.getStatusCode().value()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
    private static final String GET_STUDENT_RECORD_URL = "http://localhost:8080/api/v1/attendance/student-record";
    public static void getStudentRecord(String subjectCode, String studentId, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define URL with query parameters for subject code and student ID
        String url = GET_STUDENT_RECORD_URL + "?subjectCode=" + subjectCode + "&studentId=" + studentId;

        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        try {
            // Send GET request
            ResponseEntity<StudentAttendanceRecordResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    StudentAttendanceRecordResponse.class);

            // Handle response
            handleGetStudentRecordResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private static void handleGetStudentRecordResponse(ResponseEntity<StudentAttendanceRecordResponse> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            StudentAttendanceRecordResponse studentRecord = responseEntity.getBody();
            // Handle successful response
            System.out.println("Student record: " + studentRecord);
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }


    private static final String PRINT_RECORD_URL = "http://localhost:8080/api/v1/attendance/print";
    public static void printRecord(String subjectCode, String date, String sortId, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define URL with query parameters for subject code, date, and sort ID
        String url = PRINT_RECORD_URL + "?subjectCode=" + subjectCode + "&date=" + date + "&sort_id=" + sortId;

        // Create HttpEntity with headers
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        try {
            // Send GET request
            ResponseEntity<ByteArrayResource> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    ByteArrayResource.class);

            // Handle response
            handlePrintRecordResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private static void handlePrintRecordResponse(ResponseEntity<ByteArrayResource> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            ByteArrayResource excelSheet = responseEntity.getBody();
            // Handle successful response
            System.out.println("Excel sheet received. Saving record...");
            assert excelSheet != null;
            saveByteArrayResourceToFile(excelSheet, "student_record.xlsx");
            System.out.println("Record saved successfully.");
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }
    private static void saveByteArrayResourceToFile(ByteArrayResource resource, String filename) {
        try (OutputStream outputStream = new FileOutputStream(filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resource.getInputStream().read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
