package com.unilorin.attendance_system.authentication_api.util.dto.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unilorin.attendance_system.authentication_api.constants.Role;
import com.unilorin.attendance_system.authentication_api.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationUserFromDb {
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

    @JsonProperty("subjects")
    private Set<Subject> subjects;

    @JsonProperty("isAccountNonExpired")
    private boolean isAccountNonExpired;

    @JsonProperty("isAccountNonLocked")
    private boolean isAccountNonLocked;

    @JsonProperty("isCredentialsNonExpired")
    private boolean isCredentialsNonExpired;

    @JsonProperty("isEnabled")
    private boolean isEnabled;

    @JsonProperty("userRole")
    private List<Role> userRole;
}
