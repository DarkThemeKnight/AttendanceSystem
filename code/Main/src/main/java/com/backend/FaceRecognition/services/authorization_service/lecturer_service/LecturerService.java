package com.backend.FaceRecognition.services.authorization_service.lecturer_service;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.*;
import com.backend.FaceRecognition.repository.SuspensionRepository;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import com.backend.FaceRecognition.utils.student.StudentRequest;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LecturerService {
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final SuspensionRepository suspensionRepository;
    private final JwtService jwtService;
    private final ApplicationUserService applicationUserService;

    public LecturerService(AttendanceService attendanceService, StudentService studentService, SubjectService subjectService, SuspensionRepository suspensionRepository, JwtService jwtService, ApplicationUserService applicationUserService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.suspensionRepository = suspensionRepository;
        this.jwtService = jwtService;
        this.applicationUserService = applicationUserService;
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
    private boolean cantPerformOperation(String authorizationHeader, Subject subject){
        String jwt_Token = jwtService.extractTokenFromHeader(authorizationHeader);
        String lecturerId = jwtService.getId(jwt_Token);
        return !subject.getLecturerInCharge().getId().equals(lecturerId);
    }

    public ResponseEntity<String> clearSubjectStudents(String subjectCode,String auth) {
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>("subject Not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = optionalSubject.get();
        if (cantPerformOperation(auth, subject)){
            return new ResponseEntity<>("Unauthorized",HttpStatus.UNAUTHORIZED);
        }
        subjectService.save(subject);
        return new ResponseEntity<>("Cleared", HttpStatus.OK);
    }
    public ResponseEntity<Response> suspendStudentFromMarkingAttendance(String auth,String subjectCode, String studentId){
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>(new Response("subject Not found"), HttpStatus.NOT_FOUND);
        }
        if (cantPerformOperation(auth, optionalSubject.get())){
            return new ResponseEntity<>(new Response("Unauthorized"),HttpStatus.UNAUTHORIZED);
        }
        if (suspensionRepository.findByStudentIdAndSubjectId(studentId,subjectCode).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Already suspended"));
        }
        Suspension suspension = new Suspension();
        suspension.setStudentId(studentId);
        suspension.setSubjectId(subjectCode);
        suspensionRepository.save(suspension);
        return ResponseEntity.ok(new Response("Suspended successfully"));
    }
    public  ResponseEntity<StudentAttendanceRecordResponse> viewAttendanceRecord
            (
                                                                String auth,
                                                                String studentId,
                                                                String subjectCode
            ){
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (cantPerformOperation(auth, optionalSubject.get())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<List<Attendance>> response = attendanceService
                .getStudentRecord(studentId);
        if (response.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.notFound().build();
        }
        List<Attendance> attendanceList = response.getBody();
        if (attendanceList == null) {
            return ResponseEntity.internalServerError().build();
        }
        List<StudentAttendanceRecordResponse.DefaultResponse> getDefault =
                attendanceList.stream().filter(Objects::nonNull).filter(attendance -> attendance.getSubjectId().equalsIgnoreCase(subjectCode)).map(attendance ->
                   {
                    Subject subject= subjectService.findSubjectByCode(attendance.getSubjectId()).orElse(null);
                    return subject!=null?new StudentAttendanceRecordResponse.DefaultResponse(subject.getSubjectCode(),subject.getSubjectTitle(),
                            attendance.getDate(), attendance.getStatus()):null;
                   }
                ).toList();

        return ResponseEntity.ok(new StudentAttendanceRecordResponse(attendanceList.get(0).getStudentId(), getDefault));
    }
    @Transactional
    public ResponseEntity<Response> addStudentToSubject(String auth,StudentRequest requestSet, String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>(new Response("Subject not found"), HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        if (cantPerformOperation(auth, subject)){
            return new ResponseEntity<>(new Response("Unauthorized"),HttpStatus.UNAUTHORIZED);
        }
        Student student = studentService
                .getStudentById(requestSet.getStudent_id()).orElse(null);
        if (student == null) {
            return new ResponseEntity<>(new Response("Student not found"), HttpStatus.NOT_FOUND);
        }
        student.add(subject);
        studentService.saveStudent(student);
        subjectService.save(subject);
        return new ResponseEntity<>(new Response("Student set successfully"), HttpStatus.OK);
    }

}
