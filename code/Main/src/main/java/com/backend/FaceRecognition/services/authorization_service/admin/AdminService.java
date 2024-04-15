package com.backend.FaceRecognition.services.authorization_service.admin;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {

    private final ApplicationUserService applicationUserService;
    private final SubjectService subjectService;
    private final StudentService studentService;

    public AdminService(ApplicationUserService applicationUserService, SubjectService subjectService,
                        StudentService studentService) {
        this.applicationUserService = applicationUserService;
        this.subjectService = subjectService;
        this.studentService = studentService;
    }
    public ResponseEntity<String> lockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN) || user.hasRole(Role.ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to lock account.");
            }
            user.setEnabled(false);
            applicationUserService.update(user);
            return ResponseEntity.ok("Account locked successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    public ResponseEntity<String> unlockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN) || user.hasRole(Role.ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to lock account.");
            }
            user.setEnabled(true);
            applicationUserService.update(user);
            return ResponseEntity.ok("Account unlocked successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    public ResponseEntity<String> deleteSubject(String request) {
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request);
        if (subject1.isEmpty()) {
            return new ResponseEntity<>("Subject does not exist", HttpStatus.CONFLICT);
        }
        subjectService.deleteSubjectByCode(request);
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }
    public ResponseEntity<SubjectResponse> getSubject(String subjectCode) {
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        log.info("getting subject");
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>(new SubjectResponse("message"),
                    HttpStatus.NOT_FOUND);
        }
        SubjectResponse response = parse(optionalSubject.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    public SubjectResponse parse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setSubjectCode(subject.getSubjectCode());
        response.setSubjectTitle(subject.getSubjectTitle());
        Set<Student> students = studentService
                .getAllStudentsOfferingCourse(subject.getSubjectCode());
        // log.info("size {}",students.size());
        Set<String> matriculationNum = students.stream()
                .map(Student::getMatriculationNumber).collect(Collectors.toSet());
        response.setStudents(matriculationNum);
        if (subject.getLecturerInCharge() != null) {
            response.setIdLecturerInCharge(subject.getLecturerInCharge().getId());
        }
        return response;
    }
    public ResponseEntity<String> clearAllStudentSubjects() {
        List<Student> students = studentService.getAllStudents();
        students.forEach(Student::clear);
        studentService.saveAll(students);
        return new ResponseEntity<>("Cleared successfully", HttpStatus.OK);
    }


}
