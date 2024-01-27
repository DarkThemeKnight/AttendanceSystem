package com.unilorin.attendance_system.authentication_api.repo;

import com.unilorin.attendance_system.authentication_api.entity.ApplicationUser;
import com.unilorin.attendance_system.authentication_api.constants.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, String> {
    ApplicationUser findBySchoolEmail(String email);
}
