package com.unilorin.attendance_system.authentication_api.repo;

import com.unilorin.attendance_system.authentication_api.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student,String> {

}
