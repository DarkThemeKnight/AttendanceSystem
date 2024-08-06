package demo.api.Data.Persistence.Api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import demo.api.Data.Persistence.Api.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    @JsonProperty("message")
    String message;
    @JsonProperty("jwt_token")
    String jwtToken;
    @JsonProperty("user_roles")
    Set<Role> roles;
    LocalDateTime expiryDate;

    public AuthenticationResponse(String message, String token, Set<Role> userRole) {
        this.message = message;
        this.jwtToken = token;
        roles= userRole;
    }
}
