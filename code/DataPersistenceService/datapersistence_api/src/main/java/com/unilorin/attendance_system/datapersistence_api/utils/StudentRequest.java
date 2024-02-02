package com.unilorin.attendance_system.datapersistence_api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unilorin.attendance_system.datapersistence_api.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
@NoArgsConstructor
@Data
@AllArgsConstructor
public class StudentRequest {
    @JsonProperty("id")
    private String matriculationNumber;
    @JsonProperty("email")
    private String schoolEmail;
    @JsonProperty("firstname")
    private String firstname;
    @JsonProperty("lastname")
    private String lastname;
    @JsonProperty("middle_name")
    private String middleName;
    @JsonProperty("subjects")
    private Set<String> subjects;
    @JsonProperty("image")
    private byte[] faceImage;
}
