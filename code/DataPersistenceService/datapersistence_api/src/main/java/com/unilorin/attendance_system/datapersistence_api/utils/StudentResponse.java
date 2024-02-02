package com.unilorin.attendance_system.datapersistence_api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;
@Data
public class StudentResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("image")
    private byte[] image;
    @JsonProperty("subjects")
    private Set<SubjectResponseDto> subject;
}
