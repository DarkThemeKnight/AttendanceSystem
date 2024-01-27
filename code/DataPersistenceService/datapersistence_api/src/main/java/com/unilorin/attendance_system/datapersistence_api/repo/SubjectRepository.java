package com.unilorin.attendance_system.datapersistence_api.repo;

import com.unilorin.attendance_system.datapersistence_api.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject,String> {

}
