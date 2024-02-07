package com.backend.FaceRecognition.services.authorization_service.admin;
import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import com.backend.FaceRecognition.services.student.AddStudentRequest;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final ApplicationUserService applicationUserService;
    private final SubjectService subjectService;
    private final StudentService studentService;
    @Autowired
    public AdminService(ApplicationUserService applicationUserService, SubjectService subjectService, StudentService studentService) {
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
            return ResponseEntity.ok("Account locked successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Processes a set of SubjectRequest objects and adds valid subjects to the system.
     * Invalid subjects will be skipped.
     *
     * @param subjectSet A set of SubjectRequest objects to be processed and added.
     * @return ResponseEntity<String> An HTTP response entity indicating the outcome of the operation.
     *         - HttpStatus.OK if the valid subjects are successfully added.
     */
    public ResponseEntity<String> subjectRequest(Set<SubjectRequest> subjectSet) {
        Set<Subject> subjects = subjectSet.stream()
                .map(this::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        subjectService.addAll(subjects);
        return new ResponseEntity<>("Successfully added valid subjects", HttpStatus.OK);
    }
    public ResponseEntity<String> addSubject(SubjectRequest request){
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isPresent()){
            return new ResponseEntity<>("Subject already added",HttpStatus.CONFLICT);
        }
        Subject subject = parse(request);
        if (subject == null){
            return new ResponseEntity<>("Lecturer not found",HttpStatus.NOT_FOUND);
        }
        subjectService.saveSubject(subject);
        return new ResponseEntity<>("Saved successfully",HttpStatus.OK);
    }
    public ResponseEntity<String> updateSubject(SubjectRequest request){
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isEmpty()){
            return new ResponseEntity<>("Subject does not exist",HttpStatus.CONFLICT);
        }
        ApplicationUser user = null;
        if (request.getIdLecturerInCharge() != null && !request.getIdLecturerInCharge().isEmpty()) {
            user = applicationUserService.findUser(request.getIdLecturerInCharge()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>("Lecturer not found", HttpStatus.NOT_FOUND);
            }
        }
        Subject subject = new Subject();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectTitle(request.getSubjectTitle());
        subject.setLecturerInCharge(user);
        subjectService.saveSubject(subject);
        return new ResponseEntity<>("Saved successfully",HttpStatus.OK);
    }
    public ResponseEntity<String> deleteSubject(SubjectRequest request){
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isEmpty()){
            return new ResponseEntity<>("Subject does not exist",HttpStatus.CONFLICT);
        }
        subjectService.deleteSubjectByCode(request.getSubjectCode());
        return new ResponseEntity<>("Deleted successfully",HttpStatus.OK);
    }
    public ResponseEntity<SubjectResponse> getSubject(String subjectCode){
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()){
            return new ResponseEntity<>(new SubjectResponse("message"),HttpStatus.NOT_FOUND);
        }
        SubjectResponse response = parse(optionalSubject.get());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    public Subject parse(SubjectRequest request){
        ApplicationUser user = null;
        if (request.getIdLecturerInCharge() != null && !request.getIdLecturerInCharge().isEmpty()) {
            user = applicationUserService.findUser(request.getIdLecturerInCharge()).orElse(null);
            if (user == null) {
                return null;
            }
        }
        Subject subject = new Subject();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectTitle(request.getSubjectTitle());
        subject.setLecturerInCharge(user);
        return subject;
    }
    public SubjectResponse parse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setSubjectCode(subject.getSubjectCode());
        response.setSubjectTitle(subject.getSubjectTitle());
        if (subject.getLecturerInCharge() != null) {
            response.setIdLecturerInCharge(subject.getLecturerInCharge().getId());
        }
        response.setStudents(subject.getStudents());
        return response;
    }
    public ResponseEntity<String> clearSubjectStudents(String subjectCode){
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()){
            return new ResponseEntity<>("subject Not found",HttpStatus.NOT_FOUND);
        }
        Subject subject = optionalSubject.get();
        subject.clearStudents();
        subjectService.saveSubject(subject);
        return new ResponseEntity<>("Cleared",HttpStatus.OK);
    }
    public ResponseEntity<String> clearAllStudentSubjects(){
        List<Subject> subjects = subjectService.findAllSubjects();
        subjects.forEach(Subject::clearStudents);
        subjectService.saveSubject(subjects);
        return new ResponseEntity<>("Cleared successfully",HttpStatus.OK);
    }
    public ResponseEntity<String> addStudent(AddStudentRequest request){
        Optional<Student> studentOptional= studentService.getStudentById(request.getMatriculationNumber());
        if (studentOptional.isEmpty()) {
            Student student = parse(request);
            studentService.saveStudent(student);
            return new ResponseEntity<>("Saved",HttpStatus.OK);
        }
        return new ResponseEntity<>("Student already present",HttpStatus.CONFLICT);
    }
    public ResponseEntity<String> addStudentImage(MultipartFile file, String studentId){
        Optional<Student> studentOptional = studentService.getStudentById(studentId);
        if (studentOptional.isEmpty()){
            return new ResponseEntity<>("Student Not found",HttpStatus.NOT_FOUND);
        }
        Student student = studentOptional.get();
        try {
            // Convert the multipart file to bytes and set it as the student's face image
            student.setFaceImage(file.getBytes());
        } catch (IOException e) {
            // Handle IO exception, return internal server error
            return new ResponseEntity<>("Error processing the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        studentService.saveStudent(student);
        return new ResponseEntity<>("Saved successfully",HttpStatus.OK);
    }
    public Student parse(AddStudentRequest request){
        Student student = new Student();
        student.setFirstname(request.getFirstname());
        student.setLastname(request.getLastname());
        student.setMatriculationNumber(request.getMatriculationNumber());
        student.setMiddleName(request.getMiddleName());
        student.setSchoolEmail(request.getSchoolEmail());
        return student;
    }

}
