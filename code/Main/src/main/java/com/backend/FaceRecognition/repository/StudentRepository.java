package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student,String> {
    List<Student> findAllByFaculty(String faculty);
    List<Student> findAllByDepartment(String department);
}
