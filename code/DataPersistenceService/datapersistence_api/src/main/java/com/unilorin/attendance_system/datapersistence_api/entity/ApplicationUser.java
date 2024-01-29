package com.unilorin.attendance_system.datapersistence_api.entity;
import com.unilorin.attendance_system.datapersistence_api.constants.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class ApplicationUser implements UserDetails {
    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String middleName;
    private String schoolEmail;
    private String password;
    @OneToMany(mappedBy = "lecturerInCharge",cascade = CascadeType.ALL)
    private Set<Subject> subjects;
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class)
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
        return schoolEmail;
    }
}