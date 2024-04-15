package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.AttendanceSetupPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceSetupPolicyRepository extends JpaRepository<AttendanceSetupPolicy,Integer> {
    Optional<AttendanceSetupPolicy> findBySubjectIdAndAttendanceDate(String subjectId, LocalDate date);
}
