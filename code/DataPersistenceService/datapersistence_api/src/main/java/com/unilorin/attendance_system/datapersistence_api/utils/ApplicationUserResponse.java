package com.unilorin.attendance_system.datapersistence_api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unilorin.attendance_system.datapersistence_api.constants.Role;
import lombok.*;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ApplicationUserResponse {
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
    @JsonProperty("in_charge_of")
    private Set<SubjectResponseDto> subjects;
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

    // Constructors

}
