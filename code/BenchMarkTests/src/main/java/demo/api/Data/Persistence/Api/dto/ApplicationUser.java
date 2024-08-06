package demo.api.Data.Persistence.Api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import demo.api.Data.Persistence.Api.constants.Role;
import lombok.*;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationUser{
    @JsonIgnore
    private String id;
    private String firstname;
    private String lastname;
    private String middleName;
    private String schoolEmail;
    @JsonIgnore
    private String password;
    private String address;
    private String phoneNumber;
    private Set<Role> userRole;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;
    private String profilePictureId;
}