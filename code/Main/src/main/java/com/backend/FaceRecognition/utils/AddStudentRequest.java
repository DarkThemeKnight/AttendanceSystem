package com.backend.FaceRecognition.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStudentRequest {
    @JsonProperty("matriculation_number") //this is the id
    private String matriculationNumber;
    @JsonProperty("school_email")
    private String schoolEmail;
    private String firstname;
    private String lastname;
    @JsonProperty("middle_name")
    private String middleName;
}
