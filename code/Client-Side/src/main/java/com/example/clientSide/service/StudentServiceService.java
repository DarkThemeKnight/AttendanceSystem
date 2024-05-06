package com.example.clientSide.service;

import com.example.clientSide.utils.Response;
import com.example.clientSide.utils.StudentAttendanceRecordResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StudentServiceService {
    private static final String VIEW_ATTENDANCE_RECORDS_URL = "http://localhost:8080/api/v1/students/view";

    public void viewAttendanceRecords(String subjectCode, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define URL with query parameter for subject code
        String url = VIEW_ATTENDANCE_RECORDS_URL + "?subjectCode=" + subjectCode;

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
            handleViewAttendanceRecordsResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }

    private void handleViewAttendanceRecordsResponse(ResponseEntity<StudentAttendanceRecordResponse> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            StudentAttendanceRecordResponse attendanceRecord = responseEntity.getBody();
            // Handle successful response
            System.out.println("Attendance records: " + attendanceRecord);
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }


    private static final String ADD_STUDENT_IMAGE_URL = "http://localhost:8080/api/v1/students/image";
    public void addStudentImage(File file, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // Create MultiValueMap for form data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        // Create HttpEntity with headers and form data
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
        try {
            // Send POST request
            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                    ADD_STUDENT_IMAGE_URL,
                    HttpMethod.POST,
                    httpEntity,
                    Response.class);
            // Handle response
            handleAddStudentImageResponse(responseEntity);
        } catch (HttpClientErrorException ex) {
            // Handle HTTP client errors (4xx)
            HttpStatusCode statusCode = ex.getStatusCode();
            System.out.println("Error: " + statusCode + ", " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            // Handle other exceptions
            System.out.println("Unexpected error: " + ex.getMessage());
        }
    }
    private void handleAddStudentImageResponse(ResponseEntity<Response> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            Response response = responseEntity.getBody();
            // Handle successful response
            System.out.println("Student image added: " + response.getMessage());
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }
    private static final String PRINT_RECORD_URL = "http://localhost:8080/api/v1/students/print";
    public void printRecord(String subjectCode, String bearerToken) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Set headers with authorization bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define URL with query parameter for subject code
        String url = PRINT_RECORD_URL + "?subjectCode=" + subjectCode;

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

    private void handlePrintRecordResponse(ResponseEntity<ByteArrayResource> responseEntity) {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            ByteArrayResource excelSheet = responseEntity.getBody();
            // Handle successful response
            System.out.println("Excel sheet received. Saving record...");
            saveByteArrayResourceToFile(excelSheet, "student_record.xlsx");
            System.out.println("Record saved successfully.");
        } else {
            // Handle unexpected response
            System.out.println("Unexpected response: " + statusCode);
        }
    }

    private void saveByteArrayResourceToFile(ByteArrayResource resource, String filename) {
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
