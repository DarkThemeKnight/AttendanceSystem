package com.unilorin.attendance_system.datapersistence_api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@Data
@NoArgsConstructor
public class SubjectResponseDto {
    @JsonProperty("subject_code")
    private String subjectCode;
    @JsonProperty("subject_title")
    private String subjectTitle;
}
