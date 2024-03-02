package com.backend.FaceRecognition.services.authorization_service.advisor;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.student.StudentRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class Advisor {
    private final SubjectService subjectService;
    private final StudentService studentService;

    public Advisor(SubjectService subjectService, StudentService studentService) {
        this.subjectService = subjectService;
        this.studentService = studentService;
    }
    /**
     * Adds a student to a subject.
     * This method associates a student with a subject
     * by adding the student to the subject's list of enrolled students
     * and updating the student's list of subjects.
     * Both the student and subject entities are updated and saved.
     *
     * @param requestSet   The request containing the details of the student to be added to the subject.
     * @param subjectCode  The code of the subject to which the student is to be added.
     * @return A ResponseEntity indicating the result of the operation.
     *         If the subject with the provided code is not found, a not found response is returned.
     *         If the student with the provided ID is not found, a not found response is returned.
     *         If the student is successfully added to the subject, an OK response is returned.
     */
    @Transactional
    public ResponseEntity<String> addStudentToSubject(StudentRequest requestSet, String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        Student student = studentService.getStudentById(requestSet.getMatriculationNumber()).orElse(null);
        if (student == null) {
            return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
        }
        student.add(subject);
        studentService.saveStudent(student);
        subject.addStudent(student);
        subjectService.saveSubject(subject);
        return new ResponseEntity<>("Student set successfully", HttpStatus.OK);
    }


}
