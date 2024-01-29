package com.unilorin.attendance_system.datapersistence_api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unilorin.attendance_system.datapersistence_api.entity.Subject;

import java.util.Set;

public class UpdateRequest {
    @JsonProperty("password")
    private String password;
    @JsonProperty("subjects")
    private Set<Subject> subjects;

}
