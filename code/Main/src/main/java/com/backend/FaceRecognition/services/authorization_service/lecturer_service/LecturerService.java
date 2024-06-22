package com.backend.FaceRecognition.services.authorization_service.lecturer_service;

import com.backend.FaceRecognition.entities.*;
import com.backend.FaceRecognition.repository.SuspensionRepository;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import com.backend.FaceRecognition.utils.student.StudentRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LecturerService {
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final SuspensionRepository suspensionRepository;
    private final JwtService jwtService;

    public LecturerService(AttendanceService attendanceService, StudentService studentService, SubjectService subjectService, SuspensionRepository suspensionRepository, JwtService jwtService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.suspensionRepository = suspensionRepository;
        this.jwtService = jwtService;
    }
    public ResponseEntity<SubjectResponse> getSubject(String subjectCode,String bearer) {
           Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
           log.info("getting subject");
           if (optionalSubject.isEmpty()) {
               return new ResponseEntity<>(new SubjectResponse("Subject not found"),
                       HttpStatus.NOT_FOUND);
           }
        if(cantPerformOperation(bearer,optionalSubject.get())) {
            SubjectResponse response = parse(optionalSubject.get());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    private boolean cantPerformOperation(String authorizationHeader, Subject subject){
        String jwt_Token = jwtService.extractTokenFromHeader(authorizationHeader);
        String lecturerId = jwtService.getId(jwt_Token);
        return !subject.getLecturerInCharge().getId().equals(lecturerId);
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
    public ResponseEntity<Response> suspendStudentFromMarkingAttendance(String auth,String subjectCode, String studentId,boolean suspend){
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>(new Response("subject Not found"), HttpStatus.NOT_FOUND);
        }
        if (cantPerformOperation(auth, optionalSubject.get())){
            return new ResponseEntity<>(new Response("Unauthorized"),HttpStatus.UNAUTHORIZED);
        }
        if (suspend) {
            if (suspensionRepository.findByStudentIdAndSubjectId(studentId, subjectCode).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Already suspended"));
            }
            Suspension suspension = new Suspension(null, studentId, subjectCode);
            suspensionRepository.save(suspension);
            return ResponseEntity.ok(new Response("Suspended successfully"));
        }
        else {
            var optional = suspensionRepository.findByStudentIdAndSubjectId(studentId, subjectCode);
            if (optional.isPresent()) {
                suspensionRepository.delete(optional.get());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Restored"));
            }
            else {
                return ResponseEntity.ok(new Response("Is not a Suspended Student"));
            }
        }
    }
    public  ResponseEntity<StudentAttendanceRecordResponse>
    viewAttendanceRecord(String auth, String studentId, String subjectCode){
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
    public ResponseEntity<Response> addStudentToSubject(String auth,
                                                        String requestSet,
                                                        String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>(new Response("Subject not found"), HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        if (cantPerformOperation(auth, subject)){
            return new ResponseEntity<>(new Response("Unauthorized"),HttpStatus.UNAUTHORIZED);
        }
        Student student = studentService
                .getStudentById(requestSet).orElse(null);
        if (student == null) {
            return new ResponseEntity<>(new Response("Student not found"), HttpStatus.NOT_FOUND);
        }
        student.add(subject);
        studentService.saveStudent(student);
        subjectService.save(subject);
        return new ResponseEntity<>(new Response("Student set successfully"), HttpStatus.OK);
    }

}
