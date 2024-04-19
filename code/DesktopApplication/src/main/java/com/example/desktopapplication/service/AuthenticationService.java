package com.example.desktopapplication.authentication;

import com.example.desktopapplication.State.ApplicationContext;
import com.example.desktopapplication.dto.LoginRequest;
import com.example.desktopapplication.dto.LoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

public class AuthenticationService {
    private static final Logger log = Logger.getLogger(AuthenticationService.class.getName());
    public static void login(String id, String password) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        try(HttpClient httpClient = HttpClient.newHttpClient()) {
            String url = "http://localhost:8080/api/v1/auth/login";
            String json = objectMapper.writeValueAsString(new LoginRequest(id,password));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                LoginResponse loginResponse = objectMapper.readValue(body, LoginResponse.class);
                log.info(loginResponse.getMessage());
                ApplicationContext context = ApplicationContext.getInstance();
                context.setJwtToken(loginResponse.getJwtToken());
                context.setUserRoles(loginResponse.getUserRoles().toArray(new String[0]));
            }
            log.warning("Login failed with status code: "+ response.statusCode());
        }
    }
    public static void logout() throws IOException, InterruptedException {
        ApplicationContext context =ApplicationContext.getInstance();
        String jwtToken = context.getJwtToken();
        try(HttpClient client = HttpClient.newHttpClient()){
            HttpRequest request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .headers("Authorization","Bearer "+jwtToken)
                    .header("Content-Type", "application/json")
                    .uri(URI.create("http://localhost:8080/api/v1/auth/logout"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                log.info(response.body());
                ApplicationContext.clearInstance();
            }
        }
    }
}
