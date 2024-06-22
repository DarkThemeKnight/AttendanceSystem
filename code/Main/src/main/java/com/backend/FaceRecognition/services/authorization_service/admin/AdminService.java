package com.backend.FaceRecognition.services.authorization_service.admin;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.GetListOfUsers;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.subject.AllSubjects;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
    public ResponseEntity<String> lockAccount(String id, String bearer){
        return changeAccountStatus(id, bearer,true);
    }
    public ResponseEntity<String> unlockAccount(String id, String bearer){
        return changeAccountStatus(id, bearer,false);
    }
    public ResponseEntity<String> changeAccountStatus(String id, String bearer, boolean lock) {
        String jwt_token = jwtService.extractTokenFromHeader(bearer);
        String user_id = jwtService.getId(jwt_token);
        ApplicationUser requestingUser = applicationUserService.findUser(user_id).get();
        Set<Role> roles = requestingUser.getUserRole();
        log.info("Role => {}", roles);

        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (!roles.contains(Role.ROLE_SUPER_ADMIN)) {
            if (userOptional.isPresent()) {
                ApplicationUser user = userOptional.get();
                if (user.hasRole(Role.ROLE_SUPER_ADMIN) || user.hasRole(Role.ROLE_ADMIN)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to change account status.");
                }
                user.setEnabled(!lock); // false to lock, true to unlock
                applicationUserService.update(user);
                return ResponseEntity.ok(lock ? "Account locked successfully." : "Account unlocked successfully.");
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            if (userOptional.isPresent()) {
                ApplicationUser user = userOptional.get();
                if (user.hasRole(Role.ROLE_SUPER_ADMIN)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Unauthorized to change account status.");
                }
                user.setEnabled(!lock); // false to lock, true to unlock
                applicationUserService.update(user);
                return ResponseEntity.ok(lock ? "Account locked successfully." : "Account unlocked successfully.");
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
        Set<Student> students = studentService.getAllStudentsOfferingCourse(request);
        students.forEach(student -> {
            Set<Subject> subjects = student.getSubjects();
            // Remove the subject from the set
            subjects.removeIf(subject -> subject.getSubjectCode().equals(request));
        });
        studentService.saveAll(students);
        subjectService.deleteSubjectByCode(request);
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    public ResponseEntity<AllSubjects> getAllSubject(boolean student) {
        List<Subject> subjects = subjectService.findAll();
        if (student) {
            return ResponseEntity.ok(new
                    AllSubjects(subjects.stream().map(this::parse).collect(Collectors.toList())));
        }
        List<SubjectResponse> myList = subjects.stream()
                .filter(Objects::nonNull)
                .map(s -> SubjectResponse
                        .builder()
                        .subjectTitle(s.getSubjectTitle())
                        .idLecturerInCharge(s.getLecturerInCharge() == null ? "" : s.getLecturerInCharge().getId())
                        .subjectCode(s.getSubjectCode())
                        .build())
                .toList();
        AllSubjects allSubjectsNoStudentData = new AllSubjects(myList);
        return ResponseEntity.ok(allSubjectsNoStudentData);
    }

    public ResponseEntity<SubjectResponse> getSubject(String subjectCode) {
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        log.info("getting subject");
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>(new SubjectResponse("Subject not found"),
                    HttpStatus.NOT_FOUND);
        }
        SubjectResponse response = parse(optionalSubject.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public SubjectResponse parse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setSubjectCode(subject.getSubjectCode());
        response.setSubjectTitle(subject.getSubjectTitle());
        response.setIdLecturerInCharge(
                subject.getLecturerInCharge() == null ? "" : subject.getLecturerInCharge().getId());
        Set<Student> students = studentService
                .getAllStudentsOfferingCourse(subject.getSubjectCode());
        Set<SubjectResponse.Metadata> matriculationNum = students.
                stream()
                .map(v -> SubjectResponse.Metadata.builder()
                        .studentId(v.getMatriculationNumber())
                        .firstname(v.getFirstname())
                        .lastname(v.getLastname())
                        .build()
                ).collect(Collectors.toSet());
        response.setStudents(matriculationNum);
        response.setMessage("Fetched Successfully");
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
        String title = request.getSubjectTitle() == null ? subject.getSubjectTitle() : request.getSubjectTitle();
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
    public ResponseEntity<ApplicationUser> getUser(String userId,String bearer) {
        Optional<ApplicationUser> applicationUserOptional = applicationUserService.findUser(userId);
        String id = jwtService.getId(jwtService.extractTokenFromHeader(bearer));
        var user = applicationUserService.findUser(id).get();
        if (applicationUserOptional.get().getUserRole().contains(Role.ROLE_SUPER_ADMIN) && !user.getUserRole().contains(Role.ROLE_SUPER_ADMIN)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return applicationUserOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    public ResponseEntity<GetListOfUsers> getAllStudents() {
        List<ApplicationUser> users = applicationUserService.findAllUsers();
        users = users.stream().filter(user -> user.hasRole(Role.ROLE_STUDENT)).toList();
        List<ApplicationUserRequest> userx = users.stream().map(v -> ApplicationUserRequest.builder()
                .id(v.getId())
                .firstname(v.getFirstname())
                .lastname(v.getLastname())
                .middleName(v.getMiddleName())
                .phoneNumber(v.getPhoneNumber())
                .accountStatus(v.isEnabled()?"ACTIVE":"INACTIVE")
                .schoolEmail(v.getSchoolEmail())
                .build()).toList();

        return ResponseEntity.ok(new GetListOfUsers(userx));
    }
    public ResponseEntity<GetListOfUsers> getAll(String lowerCase, String bearer) {
        return switch (lowerCase) {
            case "student" -> getAllStudents();
            case "instructor" -> {
                List<ApplicationUser> users = applicationUserService.findAllUsers();
                users = users.stream().filter(user -> user.hasRole(Role.ROLE_LECTURER)).toList();
                List<ApplicationUserRequest> userx = users.stream().map(v -> ApplicationUserRequest.builder()
                        .id(v.getId())
                        .firstname(v.getFirstname())
                        .lastname(v.getLastname())
                        .accountStatus(v.isEnabled()?"ACTIVE":"INACTIVE")
                        .phoneNumber(v.getPhoneNumber())
                        .middleName(v.getMiddleName())
                        .schoolEmail(v.getSchoolEmail())
                        .build()).toList();
                yield ResponseEntity.ok(new GetListOfUsers(userx));
            }
            case "admin" -> {
                var reqUser= applicationUserService.findUser(jwtService.getId(jwtService.extractTokenFromHeader(bearer))).get();
                if (!reqUser.getUserRole().contains(Role.ROLE_SUPER_ADMIN)) {
                    yield new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                List<ApplicationUser> users = applicationUserService.findAllUsers();
                users = users.stream().filter(user -> user.hasRole(Role.ROLE_ADMIN)).toList();
                List<ApplicationUserRequest> userx = users.stream().map(v -> ApplicationUserRequest.builder()
                        .id(v.getId())
                        .lastname(v.getLastname())
                        .firstname(v.getFirstname())
                        .middleName(v.getMiddleName())
                        .phoneNumber(v.getPhoneNumber())
                        .accountStatus(v.isEnabled()?"ACTIVE":"INACTIVE")
                        .schoolEmail(v.getSchoolEmail())
                        .build()).toList();
                yield ResponseEntity.ok(new GetListOfUsers(userx));
            }
            case "hardware"->{
                List<ApplicationUser> users = applicationUserService.findAllUsers();
                users = users.stream().filter(user -> user.hasRole(Role.ROLE_HARDWARE)).toList();
                List<ApplicationUserRequest> applicationUserRequestList = users.stream().map(v -> ApplicationUserRequest.builder()
                        .id(v.getId())
                        .address(v.getAddress())
                        .build()).toList();
                yield ResponseEntity.ok(new GetListOfUsers(applicationUserRequestList));
            }
            default -> ResponseEntity.badRequest().build();
        };
    }


}
