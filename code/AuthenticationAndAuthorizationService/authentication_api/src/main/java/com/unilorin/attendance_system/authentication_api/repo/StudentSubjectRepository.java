package com.unilorin.attendance_system.authentication_api.repo;

import com.unilorin.attendance_system.authentication_api.entity.StudentSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSubjectRepository extends JpaRepository<StudentSubject,Long> {
    List<StudentSubject> findByStudentMatriculationNumber(String matriculationNumber);
    List<StudentSubject> findBySubjectSubjectCode(String subjectCode);
}
