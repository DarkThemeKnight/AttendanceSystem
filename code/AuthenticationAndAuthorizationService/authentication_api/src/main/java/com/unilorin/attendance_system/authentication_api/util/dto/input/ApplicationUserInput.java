package com.unilorin.attendance_system.authentication_api.util.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserInput {
    @JsonProperty("id")
    private String id;
    @JsonProperty("password")
    private String password;
}
