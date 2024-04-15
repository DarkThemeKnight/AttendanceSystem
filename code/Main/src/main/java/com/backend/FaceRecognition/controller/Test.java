package com.backend.FaceRecognition.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/test")
public class Test {
    @GetMapping("/generated.txt")
    public ResponseEntity<ByteArrayResource> generateAndDownloadFile() throws IOException {
        // Generate the content of the file dynamically
        String fileContent = "Hello, world! This file was generated on-the-fly.";

        // Convert the content to bytes
        byte[] contentBytes = fileContent.getBytes();

        // Set the headers for the response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "generated.txt");

        // Create a ByteArrayResource from the content bytes
        ByteArrayResource resource = new ByteArrayResource(contentBytes);

        // Return the ResponseEntity with the resource and headers
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(contentBytes.length)
                .body(resource);
    }
}
