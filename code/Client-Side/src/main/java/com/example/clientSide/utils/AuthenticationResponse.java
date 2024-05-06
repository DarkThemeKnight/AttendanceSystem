package com.example.clientSide.utils;

import com.example.clientSide.constants.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class AuthenticationResponse {
    @JsonProperty("message")
    String message;
    @JsonProperty("jwt_token")
    String jwtToken;
    @JsonProperty("user_roles")
    Set<Role> roles;

    public AuthenticationResponse(String message, String token, Set<Role> userRole) {
        this.message = message;
        this.jwtToken = token;
        roles= userRole;
    }
}
