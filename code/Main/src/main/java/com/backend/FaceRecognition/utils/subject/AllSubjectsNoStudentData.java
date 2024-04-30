package com.backend.FaceRecognition.utils.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AllSubjectsNoStudentData {
    List<SubjectResponse> data;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectResponse {
        @JsonProperty("subject_code")
        private String subjectCode;
        @JsonProperty("subject_title")
        private String subjectTitle;
        @JsonProperty("id_lecturer_in_charge")
        private String idLecturerInCharge;
    }
}
