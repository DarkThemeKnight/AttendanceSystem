package com.example.clientSide.helper;

import com.example.clientSide.state.ApplicationContext;
import com.example.clientSide.utility.AuthenticationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
@Slf4j
public class Helper {
    private static final ObjectMapper objectMapper=  new ObjectMapper();
    private static final RestTemplate restTemplate= new RestTemplate();
    public String login(String username,  String password) {
        HashMap<String, String > loginDetails = new HashMap<>();
        loginDetails.put("id",username);
        loginDetails.put("password",password);
        String jsonRequestBody ;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(loginDetails);
        }catch (JsonProcessingException ex){
            log.error("Could not parse json properly");
            return "ERROR";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request= new HttpEntity<>(jsonRequestBody, headers);
        try {
            AuthenticationResponse responseBody = restTemplate.exchange("http://localhost:8080/api/v1/auth/login", HttpMethod.POST,request, AuthenticationResponse.class).getBody();
            if (responseBody == null){
                return "Failed to login";
            }
            ApplicationContext context = ApplicationContext.getInstance();
            context.setRoles(responseBody.getRoles());
            context.setToken(responseBody.getJwtToken());
            context.setTokenExpiryDate(responseBody.getExpiryDate());
            log.info("LOGGED IN SUCCESSFULLY {}",context);
            return "SUCCESS";
        }catch (HttpClientErrorException ex) {
            log.info("Login Failed {} Status code => {}", ex.getResponseBodyAsString(), ex.getStatusCode());
            return null;
        }catch (Exception e){
            log.info("service currently unavailable");
            throw new RuntimeException();
        }
    }
    public String markAttendance(byte[] imageFile, String filename, String jwtToken){
        ByteArrayResource resource = new ByteArrayResource(imageFile){
            @Override
            public String getFilename(){
                return filename;
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String,Object> dataMap = new LinkedMultiValueMap<>();
        dataMap.set("file", resource);
        HttpEntity<MultiValueMap<String,Object>> requestBody = new HttpEntity<>(dataMap,headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("http://espEndpoint:80/api/attendance",requestBody,String.class);
            return "SUCCESS";
        }catch (HttpClientErrorException ex){
            log.info("Failed to mark attendance");
            return "FAILED";
        }


    }


}
