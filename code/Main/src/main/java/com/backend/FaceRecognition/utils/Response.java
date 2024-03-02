package com.backend.FaceRecognition.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@Data
public class Response {
    @JsonProperty("message")
    private String message;
}
