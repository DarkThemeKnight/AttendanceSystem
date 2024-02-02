package com.unilorin.attendance_system.authentication_api.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String message;
    private String jwt_token;
}
