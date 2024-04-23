package com.backend.FaceRecognition.services.authorization_service.admin;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
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
    private final JwtService jwtService;

    public AdminService(ApplicationUserService applicationUserService, SubjectService subjectService,
                        StudentService studentService, JwtService jwtService) {
        this.applicationUserService = applicationUserService;
        this.subjectService = subjectService;
        this.studentService = studentService;
        this.jwtService = jwtService;
    }
    public ResponseEntity<String> lockAccount(String id,String bearer) {
        String jwt_token = jwtService.extractTokenFromHeader(bearer);
        String user_id =jwtService.getId(jwt_token);
        ApplicationUser requestingUser = applicationUserService.findUser(user_id).get();
        Set<Role> roles = requestingUser.getUserRole();
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (!roles.contains(Role.ROLE_SUPER_ADMIN)) {
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
        }else {
            if (userOptional.isPresent()) {
                ApplicationUser user = userOptional.get();
                if (user.hasRole(Role.ROLE_ADMIN)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Unauthorized to lock account.");
                }
                user.setEnabled(false);
                applicationUserService.update(user);
                return ResponseEntity.ok("Account locked successfully.");
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }
    public ResponseEntity<String> unlockAccount(String id,String bearer) {
        String jwt_token = jwtService.extractTokenFromHeader(bearer);
        String user_id = jwtService.getId(jwt_token);
        ApplicationUser requestingUser = applicationUserService.findUser(user_id).get();
        Set<Role> roles = requestingUser.getUserRole();
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (!roles.contains(Role.ROLE_SUPER_ADMIN)) {
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
        }else {
            if (userOptional.isPresent()) {
                ApplicationUser user = userOptional.get();
                if (user.hasRole(Role.ROLE_SUPER_ADMIN)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to unlock account.");
                }
                user.setEnabled(true);  // Corrected to set the account as enabled
                applicationUserService.update(user);
                return ResponseEntity.ok("Account unlocked successfully.");
            } else {
                return ResponseEntity.notFound().build();
            }
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

    public ResponseEntity<String> updateSubject(SubjectRequest request) {
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isEmpty()) {
            return new ResponseEntity<>("Subject does not exist", HttpStatus.CONFLICT);
        }
        ApplicationUser user = subject1.get().getLecturerInCharge();
        if (request.getIdLecturerInCharge() != null
                && !request.getIdLecturerInCharge().isEmpty()) {
            user = applicationUserService
                    .findUser(request.getIdLecturerInCharge())
                    .orElse(null);
            if (user == null || !user.hasRole(Role.ROLE_LECTURER)) {
                return new ResponseEntity<>("Not A Lecturer", HttpStatus.BAD_REQUEST);
            }
        }
        Subject subject = subject1.get();
        subject.setSubjectCode(request.getSubjectCode());
        String title = request.getSubjectTitle()==null?subject.getSubjectTitle():request.getSubjectTitle();
        subject.setSubjectTitle(title);
        subject.setLecturerInCharge(user);
        subjectService.save(subject);
        return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
    }
    public Subject parse(SubjectRequest request) {
        ApplicationUser user = null;
        if (request.getIdLecturerInCharge() != null) {
            user = applicationUserService.findUser(request.getIdLecturerInCharge())
                    .orElse(null);
            if (user == null || !user.hasRole(Role.ROLE_LECTURER)) {
                return null;
            }
        }
        Subject subject = new Subject();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectTitle(request.getSubjectTitle());
        subject.setLecturerInCharge(user);
        return subject;
    }

    public ResponseEntity<String> addSubject(SubjectRequest request) {
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isPresent()) {
            return new ResponseEntity<>("Subject already added", HttpStatus.CONFLICT);
        }
        Subject subject = parse(request);
        if (subject == null) {
            return new ResponseEntity<>("Not a Lecturer", HttpStatus.BAD_REQUEST);
        }
        subjectService.save(subject);
        return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
    }


}
