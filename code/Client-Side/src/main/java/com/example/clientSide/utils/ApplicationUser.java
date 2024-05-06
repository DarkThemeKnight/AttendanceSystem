package com.example.clientSide.utils;

import com.example.clientSide.constants.Role;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class ApplicationUser{
    private String id;
    private String firstname;
    private String lastname;
    private String middleName;
    private String schoolEmail;
    private String password;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    private Set<Role> userRole;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;
    private String profilePictureId;
}