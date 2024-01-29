package com.unilorin.attendance_system.datapersistence_api.utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unilorin.attendance_system.datapersistence_api.constants.Role;
import com.unilorin.attendance_system.datapersistence_api.entity.ApplicationUser;
import com.unilorin.attendance_system.datapersistence_api.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;
@AllArgsConstructor
@Builder
@Data
public class ApplicationUserAllParams {
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

    public ApplicationUserAllParams (ApplicationUser applicationUser){
        id = applicationUser.getId();
        lastname= applicationUser.getLastname();
        middleName = applicationUser.getMiddleName();
        firstname = applicationUser.getFirstname();
        schoolEmail = applicationUser.getSchoolEmail();
        password = applicationUser.getPassword();
        isAccountNonExpired = applicationUser.isAccountNonExpired();
        isCredentialsNonExpired = applicationUser.isCredentialsNonExpired();
        isAccountNonLocked = applicationUser.isAccountNonLocked();
        isEnabled = applicationUser.isEnabled();
        userRole = applicationUser.getUserRole();
        subjects = applicationUser.getSubjects();
    }
    public ApplicationUser getApplicationUser(){
        return  ApplicationUser.builder()
                .id(id)
                .lastname(lastname)
                .firstname(firstname)
                .middleName(middleName)
                .schoolEmail(schoolEmail)
                .password(password)
                .subjects(subjects)
                .isAccountNonExpired(isAccountNonExpired)
                .isAccountNonLocked(isAccountNonLocked)
                .isCredentialsNonExpired(isCredentialsNonExpired)
                .isEnabled(isEnabled)
                .userRole(userRole).build();
    }

}
