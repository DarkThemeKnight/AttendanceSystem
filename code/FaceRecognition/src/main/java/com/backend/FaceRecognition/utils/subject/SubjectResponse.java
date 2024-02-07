package com.backend.FaceRecognition.utils.subject;
import com.backend.FaceRecognition.entities.Student;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubjectResponse {
    private String message;
    @JsonProperty( "subject_code")
    private String subjectCode;
    @JsonProperty("subject_title")
    private String subjectTitle;
    @JsonProperty("id_lecturer_in_charge")
    private String idLecturerInCharge;
    @JsonProperty("students")
    private Set<Student> students = new HashSet<>();

    public SubjectResponse(String message) {
        this.message = message;
    }
}
