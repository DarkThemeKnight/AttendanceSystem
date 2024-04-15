package com.backend.FaceRecognition.services.subject;

import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }
    public Optional<Subject> findSubjectByCode(String subjectCode) {
        return subjectRepository.findById(subjectCode);
    }
    public void save(Subject subject) {subjectRepository.save(subject);}
    public void deleteSubjectByCode(String subjectCode) {
        subjectRepository.deleteById(subjectCode);
    }
}
