package com.example.clientSide.utils;

import com.example.clientSide.constants.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRecordResponse {
    @JsonProperty("Course_Title")
    String title;
    @JsonProperty("Course_code")
    String subjectCode;
    @JsonProperty("Date")
    String date;
    @JsonProperty("Attendee_Status")
    Map<String, AttendanceStatus> attendeeStatus = new HashMap<>();
    public void put(String matriculationNumber, AttendanceStatus value){
        attendeeStatus.put(matriculationNumber,value);
    }
}
