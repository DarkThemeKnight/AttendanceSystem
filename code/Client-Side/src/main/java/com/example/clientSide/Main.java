package com.example.clientSide;

import com.example.clientSide.helperClasses.Login;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;

@Slf4j
public class Main {
    private static final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl="http://localhost:8080/api/v1";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static void main(String[] args) throws IOException {
        File file = new File("FillHerUp/students");
        File[] folders = file.listFiles();
        Main main = new Main();
//        String adminToken= main.loginUser(new Login("0001","141066"));
//        main.addUser("i1","xx","ca","demo",null,"",adminToken,"instructor");
        String lecturer = main.loginUser(new Login("i1","CA"));
//        JsonNode body=objectMapper.readTree(main.addUser("hardD", null, "x", null, null, null, adminToken, "hardware"));
//        String token = body.get("message").asText().split("=")[1];
//        main.addSubject("CPE302","Demo Subject","i1",adminToken);
//        String id ="PxS";
//        int i = 0;
//        assert folders != null;
//        for (File f:folders){
//            String[] name = f.getName().split("_");
//            main.addUser(id+i,name[0],name[1],null,null,null,adminToken,"student");
//            String myToken = main.loginUser(new Login(id+i,name[1].toUpperCase()));
//            File[] images= f.listFiles();
//            assert images != null;
//            for (File image:images){
//                main.addStudentImage(image,myToken);
//            }
//            main.addStudentToSubject(id+i,"CPE302",lecturer);
//            i++;
//        }
        String hardware= main.loginUser(new Login("hardD","X"));
//        System.out.println(hardware);
//        main.initializeAttendance("CPE302",lecturer);
        String attendanceToken = "3485037638";
        file= new File("FillHerUp/attendance");
        File[] list=file.listFiles();
        assert list != null;
        for (File img:list) {
            try {
                main.updateStatus(hardware,attendanceToken,img);
            }catch (Exception ignored){
            }
        }

    }
    public void updateStatus(String token, String policy, File image) {
        ByteArrayResource resource;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(Path.of(image.getPath()))) {
                @Override
                public String getFilename() {
                    return image.getName();
                }
            };
        } catch (IOException e) {
            // Handle exception
            throw new RuntimeException("Failed to read file", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        HttpEntity<Object> entity = new HttpEntity<>(map, headers);
        String baseUrl = "http://localhost:8080";
        restTemplate.postForEntity(baseUrl + "/api/v1/hardware/update?attendanceCode=" + policy, entity, String.class);
    }


    public void initializeAttendance(String id,String token){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(token);
        HttpEntity<Void> entity=  new HttpEntity<>(httpHeaders);
        var bodyResp = restTemplate.postForEntity(baseUrl + "/attendance/initialize?subjectCode="
                +id + "&duration=" + 350, entity, String.class);
        String code =bodyResp.getBody();
        System.out.println(code);
    }
    public void addStudentToSubject(String id,String subjectId,String token){
        String myUrl = baseUrl+"/attendance/add?studentId="+id+"&subjectCode="+subjectId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            log.info("response {}",restTemplate.postForEntity(myUrl,entity,String.class));
        }catch (HttpClientErrorException i){
            log.info("{}",i.getMessage());
        }

    }
    public String getUsers(String type,String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity= new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl+"/admin/"+type,
                HttpMethod.GET,entity,
                String.class).getBody();
    }
    @SneakyThrows
    public void addSubject(String subjectId,String subjectTitle, String lecturerId,String token){
        HashMap<String,String> map = new HashMap<>();
        map.put("subject_code",subjectId);
        map.put("subject_title",subjectTitle);
        map.put("id_lecturer_in_charge",lecturerId);
        String json  = objectMapper.writeValueAsString(map);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<String> myEntity =new HttpEntity<>(json,headers);
        try {
            log.info("Response Message => {}",restTemplate.postForEntity(
                    baseUrl+"/admin/add-subject",
                    myEntity, String.class
            ).getBody());
        }catch (HttpClientErrorException e){
            log.info(e.getMessage());
        }

    }
    @SneakyThrows
    public void addStudentImage( File image,String token) {
        ByteArrayResource byteArrayResource =
                new ByteArrayResource(Files.readAllBytes(Path.of(image.getPath()))) {
            @Override
            public String getFilename() {
                return image.getName();
            }
        };
        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);
        // Prepare the body of the request
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("file", byteArrayResource);
        // Create the HttpEntity
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(bodyMap, headers);
        // Send the request
        try {
            ResponseEntity<String> responseEntity = restTemplate
                    .postForEntity(baseUrl + "/students/image", entity, String.class);
        }catch (HttpClientErrorException.BadRequest ignored){
            log.info("Bad image");
        }
    }
    @SneakyThrows
    public String addUser(String id, String firstname, String lastname, String schoolEmail, LocalDate dateOfBirth, String phoneNumber,String token,String role) {
        log.info(role);
        HashMap<String,String> map = new HashMap<>();
        map.put("id",id);
        map.put("firstName",firstname);
        map.put("lastName",lastname);
        map.put("schoolEmail",schoolEmail);
        map.put("address","addressDemo");
//        map.put("dateOfBirth",dateOfBirth.toString());
        map.put("phoneNumber",phoneNumber);
        map.put("role",role);
        String json = objectMapper.writeValueAsString(map);
        try{
            String myUrl = baseUrl+"/admin/register";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(json,headers);
            ResponseEntity<String> response = restTemplate.postForEntity(myUrl,
                    entity,String.class);
            return response.getBody();
        }catch (HttpClientErrorException exception){
            log.error("Something went wrong {}",exception.getStatusCode(),exception);
            return "";
        }

    }
    @SneakyThrows
    public String loginUser(Login login){
        try {
            String myUrl = baseUrl + "/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Login> entity = new HttpEntity<>(login, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    myUrl,
                    entity,
                    String.class
            );
            JsonNode tree= objectMapper.readTree(response.getBody());
            return tree.get("jwt_token").asText();
        }catch (HttpClientErrorException e){
            return null;
        }
    }
}
