package com.unilorin.attendance_system.authentication_api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unilorin.attendance_system.authentication_api.enumerations.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserRequest {
    @JsonProperty("id")
    private String id;
    @JsonProperty("firstname")
    private String firstname;
    @JsonProperty("lastname")
    private String lastname;
    @JsonProperty("middleName")
    private String middleName;
    @JsonProperty("schoolEmail")
    private String schoolEmail;
    @JsonProperty("password")
    private String password;
    @JsonProperty("userRole")
    private List<Role> userRole;
    @JsonProperty("isAccountNonExpired")
    private boolean isAccountNonExpired;
    @JsonProperty("isAccountNonLocked")
    private boolean isAccountNonLocked;
    @JsonProperty("isCredentialsNonExpired")
    private boolean isCredentialsNonExpired;
    @JsonProperty("isEnabled")
    private boolean isEnabled;
    @Builder
    public ApplicationUserRequest(String id, String firstname, String lastname, String middleName, String schoolEmail, String password, boolean isAccountNonExpired, boolean isAccountNonLocked, boolean isCredentialsNonExpired, boolean isEnabled) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.middleName = middleName;
        this.schoolEmail = schoolEmail;
        this.password = password;
        this.userRole = new ArrayList<>();
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
    }
}
