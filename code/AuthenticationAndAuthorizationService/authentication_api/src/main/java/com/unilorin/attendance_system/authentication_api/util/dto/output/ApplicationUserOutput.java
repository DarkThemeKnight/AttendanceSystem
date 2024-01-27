package com.unilorin.attendance_system.authentication_api.util.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ApplicationUserOutput {
    private String jwt_token;
    private String message;
}
