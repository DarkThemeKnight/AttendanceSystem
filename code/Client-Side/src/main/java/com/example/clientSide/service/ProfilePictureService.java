package com.example.clientSide.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

@Service
public final class ProfilePictureService {
    private static final RestTemplate restTemplate=new RestTemplate();
    public static ResponseEntity<byte[]> getProfilePicture(String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + jwtToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "http://localhost:8080/api/v1/profile-picture/";

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            HttpStatusCode statusCode = response.getStatusCode();
            return response;
        }catch (HttpClientErrorException ex){
            System.out.println(ex.getResponseBodyAsString()+"  "+ex.getStatusCode());
            return null;
        }
    }


    public static ResponseEntity<String>  uploadProfilePicture(String imagePath, String token) throws IOException {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(token);
            File file = new File(imagePath);
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            Resource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getName();
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", resource);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            return   restTemplate.postForEntity(
                    "http://localhost:8080/api/v1/profile-picture/",
                    requestEntity,
                    String.class);
        }catch (HttpClientErrorException ex){
            return new ResponseEntity<>(ex.getResponseBodyAsString(),ex.getStatusCode());
        }
    }
}