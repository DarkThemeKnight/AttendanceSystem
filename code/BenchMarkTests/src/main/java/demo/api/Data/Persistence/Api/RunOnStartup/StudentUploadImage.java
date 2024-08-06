package demo.api.Data.Persistence.Api.RunOnStartup;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.api.Data.Persistence.Api.dto.AuthenticationRequest;
import demo.api.Data.Persistence.Api.dto.AuthenticationResponse;
import demo.api.Data.Persistence.Api.dto.Response;
import demo.api.Data.Persistence.Api.dto.SubjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StudentUploadImage {
    private static final String LOGIN_URL = "http://localhost:8080/api/v1/auth/login";
    File imagesDir = new File("/home/omotola/Documents/code/lfw_funneled");
    ObjectMapper objectMapper = new ObjectMapper();
    RestTemplate restTemplate = new RestTemplate();
    private final int size = 200;
    private void setupSubject(String token){
        String add = "http://localhost:8080/api/v1/admin/add-subject";
        Map<String,String> map = new HashMap<>(Map.of("subject_code","CPE500",
                "subject_title","Computer Systems",
                "id_lecturer_in_charge","i0001"));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.setBearerAuth(token);

            String json = objectMapper.writeValueAsString(map);
            HttpEntity<String> entity = new HttpEntity<>(json,headers);
            var response = restTemplate.postForEntity(add,entity,String.class);
            log.info("Add Subject response {}",response);

        }catch (Exception e){
            log.info("Error",e);
            return;
        }
    }
    private void addUser(String firstname,String lastname, String id, String email, String token){
        Map<String,String> map = new HashMap<>(Map.of(
                "id",id,
                "firstName",firstname,
                "lastName",lastname,
                "schoolEmail",email,
                "role","student"
        ));
        try {
            String json = objectMapper.writeValueAsString(map);
            String add = "http://localhost:8080/api/v1/admin/register";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(json,headers);
            var response = restTemplate.postForEntity(add,entity,String.class);
            log.info("Add Student response {}",response);
        }catch (Exception e){
            log.error("Error occurred",e);
            return;
        }
    }
    private void initialSetup(AuthenticationResponse adminDetails, AuthenticationResponse lecturer){
        setupSubject(adminDetails.getJwtToken());
        List<File> files=Arrays.stream(Objects.requireNonNull(imagesDir.listFiles())).filter(File::isDirectory).collect(Collectors.toList());
        files
                .forEach(file -> {
                    String lastname;
                    String firstname;
                    String[] names = file.getName().split("_");
                    if (names.length != 2){
                        return;
                    }
                    firstname = names[1];
                    lastname = names[0];
                    addUser(firstname,lastname,lastname+"_"+firstname,lastname+"_"+firstname+"@gmail.com",adminDetails.getJwtToken());
                    addToSubject(lecturer.getJwtToken(),lastname+"_"+firstname);
                });
    }
    private void addToSubject(String token,String id){
        String path = "http://localhost:8080/api/v1/attendance/add?studentId="+id+"&subjectCode="+ "CPE500";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            log.info(restTemplate.exchange(path,HttpMethod.POST,entity,String.class).getBody());
        }catch (Exception e){
            log.info("Error",e);
            throw new RuntimeException(e);
        }

    }
    private SubjectResponse getSubject(AuthenticationResponse lecturerDetails, String subject){
        String path = "http://localhost:8080/api/v1/attendance?subjectCode="+subject;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.setBearerAuth(lecturerDetails.getJwtToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(path,HttpMethod.GET,entity,SubjectResponse.class).getBody();
        }catch (Exception e){
            return null;
        }
    }
    @Bean
    public Integer test(){
        AuthenticationResponse adminDetails = login("0001","141066");
        AuthenticationResponse demoLecturer = login("i0001","DEMOSURNAME");
        initialSetup(adminDetails,demoLecturer);
        SubjectResponse subjectResponse = getSubject(demoLecturer,"CPE500");
        assert subjectResponse != null;
        executeTest(subjectResponse);
        log.info("Closed...........");
        return 0;
    }
    public void testAttendanceMarking(SubjectResponse subjectResponse, String code) {
        Set<SubjectResponse.Metadata> metadata = subjectResponse.getStudents();
        int numberOfThreads = 4; // Number of threads to use to speed things up.
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        String statsFilePath = "csvFiles/face_recognition/stats.csv";
        File statsFile = new File(statsFilePath);
        statsFile.getParentFile().mkdirs(); // Create directories if they don't exist
        try (FileWriter writer = new FileWriter(statsFilePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("StudentId", "LastName", "FirstName", "Status", "ResponseMessage", "ResponseCode"))) {
            for (SubjectResponse.Metadata data : metadata) {
                executor.submit(() -> {
                    File imageDir = new File(imagesDir, data.getLastname() + "_" + data.getFirstname());
                    File[] images = imageDir.listFiles();
                    assert images != null;
                    String[] response = updateStudentAttendanceWithImage(code, images[0]);
                    boolean isFalseAccept = response[1].equalsIgnoreCase(HttpStatus.OK.name())
                            && !response[0].equalsIgnoreCase(data.getStudentId());
                    boolean isFalseReject = response[1].equalsIgnoreCase(HttpStatus.NOT_FOUND.name());

                    synchronized (csvPrinter) {
                        try {
                            csvPrinter.printRecord(data.getStudentId(), data.getLastname(), data.getFirstname(),
                                    isFalseAccept ? "False Accept" : isFalseReject ? "False Reject" : "Correct",
                                    response[0], response[1]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                    System.out.println("Executor did not terminate in the specified time.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void testAttendanceMarking(SubjectResponse subjectResponse,String code){
//        Set<SubjectResponse.Metadata> metadata = subjectResponse.getStudents();
//        int totalRequest= 0;
//        int falseAccept = 0;
//        int falseReject = 0;
//        int numberOfThreads = 4; // Number of threads to use to speed things up.
//        for (SubjectResponse.Metadata data: metadata) {
//            File imageDir = new File(imagesDir, data.getLastname() + "_" + data.getFirstname());
//            File[] images = imageDir.listFiles();
//            assert images != null;
//            String[] response = updateStudentAttendanceWithImage(code,images[0]);
//            if (response[1].equalsIgnoreCase(HttpStatus.OK.name())
//                &&
//                !response[0].equalsIgnoreCase(data.getStudentId()))
//            {
//                    falseAccept++;
//                    totalRequest++;
//            }
//            if (response[1].equalsIgnoreCase(HttpStatus.NOT_FOUND.name())){
//                    falseReject++;
//                    totalRequest++;
//            }
//        }
//    }
    public void executeTest(SubjectResponse subjectResponse) {
        Set<SubjectResponse.Metadata> metadata = subjectResponse.getStudents();
//        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        int numberOfThreads = 4; // Number of threads equals the number of students
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        metadata.parallelStream().forEach(c ->
            executorService.submit(() -> {
            AuthenticationResponse student = login(c.getStudentId(), c.getLastname().toUpperCase());
            if (student != null) {
//                long startTime = System.currentTimeMillis();
                upload(student, c.getLastname(), c.getFirstname());
//                long endTime = System.currentTimeMillis();
//                responseTimes.add(endTime - startTime);
            }
        }));
        // Shutdown the executor service and wait for tasks to complete
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(120, TimeUnit.MINUTES)) {
                log.warn("Some tasks were not completed in the specified time.");
            } else {
                log.info("All tasks completed.");
//                writeStatistics(responseTimes);
            }
        } catch (InterruptedException e) {
            log.error("Executor service interrupted: ", e);
        }
    }
    private void upload(AuthenticationResponse student, String lastname, String firstname) {
        final String path = "http://localhost:8080/api/v1/students/image";
        File file = new File(imagesDir, lastname + "_" + firstname);
        Arrays.stream(Objects.requireNonNull(file.listFiles())).forEach(file1 -> {
            try {
                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(Path.of(file1.getAbsolutePath()))) {
                    @Override
                    public String getFilename() {
                        return file1.getName();
                    }
                };
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.setBearerAuth(student.getJwtToken());
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", resource);
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
                restTemplate.postForEntity(path, entity, String.class);
            } catch (HttpClientErrorException ex) {
                log.warn("Client error occurred: {} Reason: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("Error occurred during file upload: ", e);
            }
        });
    }
    private void writeStatistics(List<Long> responseTimes) {
        double mean = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        double std = Math.sqrt((double) responseTimes.stream().mapToLong(time -> (long) ((time - mean) * (time - mean))).sum() / responseTimes.size());

        try (FileWriter writer = new FileWriter("csvFiles/uploadImage/statistics_"+size+"_concurrent_users.csv");
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Metric", "Value"))) {

            csvPrinter.printRecord("Mean Response Time (ms)", mean);
            csvPrinter.printRecord("Max Response Time (ms)", max);
            csvPrinter.printRecord("Min Response Time (ms)", min);
            csvPrinter.printRecord("Standard Deviation (ms)", std);
            csvPrinter.printRecord("Total Requests", responseTimes.size());
        } catch (IOException e) {
            log.error("Error writing statistics: ", e);
        }
    }
    public AuthenticationResponse login(String id, String password) {
        // Create the login request
        AuthenticationRequest request = new AuthenticationRequest(id, password);
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        // Create HttpEntity containing the request and headers
        HttpEntity<AuthenticationRequest> httpEntity = new HttpEntity<>(request, headers);
        // Send POST request
        ResponseEntity<AuthenticationResponse> response = restTemplate.exchange(
                LOGIN_URL, HttpMethod.POST, httpEntity, AuthenticationResponse.class);
        // Return the response body
        return response.getBody();
    }

    public String[] updateStudentAttendanceWithImage(String attendanceCode, File imageFile) {
        String[] result = new String[2];
        try {
            final String url = "http://192.168.43.49:8080/api/v1/students/update?attendanceCode=" + attendanceCode;
            // Create the file resource
            ByteArrayResource fileResource = new ByteArrayResource(Files.readAllBytes(Path.of(imageFile.getAbsolutePath()))) {
                @Override
                public String getFilename() {
                    return imageFile.getName();
                }
            };
            // Prepare the request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);
            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            // Create the HTTP entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            // Send the request
            try{
                ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
                String message = objectMapper.readTree(response.getBody()).get("message").asText();
                result[0] = extractStudentIdFromMessage(message);
                result[1] = response.getStatusCode().toString();
            }catch (HttpClientErrorException exception){
                result[0] = objectMapper.readValue(exception.getResponseBodyAsString(), Response.class).getMessage();
                result[1] = exception.getStatusCode().toString();
            }
        } catch (Exception e) {
            result[0] = "Exception occurred: " + e.getMessage();
            result[1] = HttpStatus.INTERNAL_SERVER_ERROR.toString();
        }

        return result;
    }

    // Helper method to extract student ID from the message
    public String extractStudentIdFromMessage(String message) {
        String[] parts = message.split("=");
        return parts.length > 1 ? parts[1].trim() : null;
    }

}
