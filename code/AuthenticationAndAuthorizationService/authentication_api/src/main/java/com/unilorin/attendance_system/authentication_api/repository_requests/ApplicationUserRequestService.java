package com.unilorin.attendance_system.authentication_api.repository_requests;

import com.unilorin.attendance_system.authentication_api.exceptions.UserAlreadyExistsException;
import com.unilorin.attendance_system.authentication_api.utils.ApplicationUser;
import com.unilorin.attendance_system.authentication_api.utils.ApplicationUserRequest;
import com.unilorin.attendance_system.authentication_api.utils.AuthRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class ApplicationUserRequestService {
    private final String BASE_URL = "http://localhost:8080/api/v1/user/";
    RestTemplate restTemplate = new RestTemplate();
    public ApplicationUser findUser(String userId){
        String endpointUrl = BASE_URL + "get/" + userId;
        ResponseEntity<ApplicationUser> response = restTemplate.getForEntity(endpointUrl, ApplicationUser.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        else if (response.getStatusCode().isSameCodeAs(HttpStatus.NO_CONTENT)){
            throw new DisabledException("Disabled Account");
        }
        else throw new UsernameNotFoundException("User does not exist");
    }
    public ApplicationUser loginUser(AuthRequest request){
        String endpointUrl = BASE_URL + "login";
        ResponseEntity<ApplicationUser> response = restTemplate.postForEntity(endpointUrl,request,ApplicationUser.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        else if (response.getStatusCode().isSameCodeAs(HttpStatus.NO_CONTENT)){
            throw new DisabledException("Disabled Account");
        }
        else throw new UsernameNotFoundException("User does not exist");
    }
    public ApplicationUser create(ApplicationUserRequest applicationUser)throws UserAlreadyExistsException{
        String endpointUrl = BASE_URL + "create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ApplicationUserRequest> requestHttpEntity = new HttpEntity<>(applicationUser,headers);
        try {
            ResponseEntity<ApplicationUser> responseEntity = restTemplate.postForEntity(endpointUrl, requestHttpEntity, ApplicationUser.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            }
        }catch (HttpClientErrorException ignored){
        }
        throw new UserAlreadyExistsException();
    }
    public ResponseEntity<String> update(String updateType, String update, String bearerToken) {
        String endpointUrl = BASE_URL + "update?update_type=" + updateType + "&update=" + update;
        return sendPutRequest(endpointUrl, bearerToken);
    }
    private ResponseEntity<String> sendPutRequest(String endpointUrl,String bearerToken) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Authorization",bearerToken)
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<>(response.body(), HttpStatus.valueOf(response.statusCode()));
        } catch (Exception e) {
            log.error("Error occurred while sending request: {}",e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
