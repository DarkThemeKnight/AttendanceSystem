package com.backend.FaceRecognition.services.authorization_service.advisor;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.student.StudentRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Advisor {
    private final SubjectService subjectService;
    private final StudentService studentService;

    public Advisor(SubjectService subjectService, StudentService studentService) {
        this.subjectService = subjectService;
        this.studentService = studentService;
    }
    /**
     * Add students to a subject identified by the provided subject code.
     *
     * @param requestSet   A set of StudentRequest objects representing the students to be added.
     * @param subjectCode  The code of the subject to which students will be added.
     * @return ResponseEntity<String> An HTTP response entity indicating the outcome of the operation.
     *         - HttpStatus.OK if the students are successfully added to the subject.
     *         - HttpStatus.NOT_FOUND if the subject with the provided code is not found.
     */
    @Transactional
    public ResponseEntity<String> addStudentsToSubject(Set<StudentRequest> requestSet, String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isPresent()) {
            Subject subject = subjectOptional.get();
            Set<Student> students = requestSet.stream()
                    .map(studentRequest -> studentService.getStudentById(studentRequest.getMatriculationNumber()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            students.forEach(student -> student.add(subject));
            studentService.saveAll(students);
            subject.addStudent(students);
            subjectService.saveSubject(subject);
            return new ResponseEntity<>("Subject Added to Students Successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
        }
    }

}
