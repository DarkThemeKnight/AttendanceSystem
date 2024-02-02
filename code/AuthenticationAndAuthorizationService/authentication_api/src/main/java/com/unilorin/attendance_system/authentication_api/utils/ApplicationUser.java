package com.unilorin.attendance_system.authentication_api.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unilorin.attendance_system.authentication_api.enumerations.Role;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ApplicationUser implements UserDetails {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRole.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
    @Override
    public String getUsername() {
        return id;
    }

}