package com.unilorin.attendance_system.datapersistence_api.entity;
import com.unilorin.attendance_system.datapersistence_api.constants.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
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
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",joinColumns = @JoinColumn(name = "id"))
    private List<Role> userRole;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
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