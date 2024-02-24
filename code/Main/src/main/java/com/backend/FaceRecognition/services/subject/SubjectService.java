package com.backend.FaceRecognition.services.subject;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }
    public Set<Subject> findAllByLecturerInCharge(ApplicationUser lecturer) {return subjectRepository.findAllByLecturerInCharge(lecturer);}
    public Optional<Subject> findSubjectByCode(String subjectCode) {
        return subjectRepository.findById(subjectCode);
    }
    public void saveSubject(Subject subject) {subjectRepository.save(subject);}
    public void deleteSubjectByCode(String subjectCode) {
        subjectRepository.deleteById(subjectCode);
    }
    public void addAll(Set<Subject> subject){
        subjectRepository.saveAll(subject);
    }

    public List<Subject> findAllSubjects() {
        return subjectRepository.findAll();
    }

    public void saveSubject(List<Subject> subjects) {
        subjectRepository.saveAll(subjects);
    }
}
