package com.backend.FaceRecognition.entities;

import com.backend.FaceRecognition.constants.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@ToString
public class ApplicationUser implements UserDetails {
    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String middleName;
    private String schoolEmail;
    private String password;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",joinColumns = @JoinColumn(name = "id"))
    private Set<Role> userRole;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;
    private String profilePictureId;

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
    public boolean addUserRole(Role role){
        if (userRole == null){
            userRole = new HashSet<>();
        }
        return userRole.add(role);
    }
    public boolean hasRole(Role role){
        return userRole.contains(role);
    }
}