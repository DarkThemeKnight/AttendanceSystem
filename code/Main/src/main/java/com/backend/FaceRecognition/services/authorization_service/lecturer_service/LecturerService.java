package com.backend.FaceRecognition.services.authorization_service.lecturer_service;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.*;
import com.backend.FaceRecognition.repository.AttendanceSetupPolicyRepository;
import com.backend.FaceRecognition.repository.SuspensionRepository;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.ListOfSubjects;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LecturerService {
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final SuspensionRepository suspensionRepository;
    private final JwtService jwtService;
    private final ApplicationUserService applicationUserService;

    public LecturerService(AttendanceService attendanceService, StudentService studentService, SubjectService subjectService, SuspensionRepository suspensionRepository, JwtService jwtService,@Lazy ApplicationUserService applicationUserService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.suspensionRepository = suspensionRepository;
        this.jwtService = jwtService;
        this.applicationUserService = applicationUserService;
    }
    @Lazy
    @Autowired
    private AttendanceSetupPolicyRepository attendanceSetupPolicyRepository;
    public ResponseEntity<SubjectResponse> getSubject(String subjectCode,String bearer) {
           Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
           log.info("getting subject");
           if (optionalSubject.isEmpty()) {
               return new ResponseEntity<>(new SubjectResponse("Subject not found"),
                       HttpStatus.NOT_FOUND);
           }
        if(!cantPerformOperation(bearer,optionalSubject.get())) {
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
    private boolean cantPerformOperation2(String authorizationHeader, Subject subject){
//        String jwt_Token = jwtService.extractTokenFromHeader(authorizationHeader);
        String lecturerId = jwtService.getId(authorizationHeader);
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
                .map(v -> {
                    ResponseEntity<List<Attendance>> response1 =
                            attendanceService.getStudentRecord(v.getMatriculationNumber());
                    AtomicInteger score= new AtomicInteger();
                    String percentage="0";
                    if (response1.getStatusCode().is2xxSuccessful() && response1.getBody() != null) {
                        response1.getBody().stream().filter(attendance -> attendance.getSubjectId().equalsIgnoreCase(subject.getSubjectCode())).forEach(attendance -> {
                            if (attendance.getStatus().equals(AttendanceStatus.PRESENT)){
                                score.getAndIncrement();
                            }
                        });
                        percentage = String.format("%.2f", (score.get() * 100.0)/response1.getBody().size());
                    }
                    SubjectResponse.Metadata subjectResponseMetadata = SubjectResponse.Metadata.builder()
                                    .studentId(v.getMatriculationNumber())
                                    .firstname(v.getFirstname())
                                    .lastname(v.getLastname())
                                    .percentage(percentage)
                                    .build();
                    Optional<Suspension> suspension = suspensionRepository.findByStudentIdAndSubjectId(v.getMatriculationNumber(),subject.getSubjectCode());
                    subjectResponseMetadata.setSuspended(suspension.isPresent());
                    return subjectResponseMetadata;
                        }
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

    public ResponseEntity<Response> suspendStudentFromMarkingAttendance(String auth, String subjectCode, String studentId, boolean suspend) {
        log.debug("Validating subject with code: {}", subjectCode);
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);

        if (optionalSubject.isEmpty()) {
            log.warn("Subject with code: {} not found", subjectCode);
            return new ResponseEntity<>(new Response("Subject Not found"), HttpStatus.NOT_FOUND);
        }

        if (cantPerformOperation(auth, optionalSubject.get())) {
            log.warn("Unauthorized attempt to modify suspension for subject: {}", subjectCode);
            return new ResponseEntity<>(new Response("Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        if (suspend) {
            log.debug("Checking if student: {} is already suspended for subject: {}", studentId, subjectCode);
            if (suspensionRepository.findByStudentIdAndSubjectId(studentId, subjectCode).isPresent()) {
                log.info("Student: {} already suspended for subject: {}", studentId, subjectCode);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Already suspended"));
            }
            Suspension suspension = new Suspension(null, studentId, subjectCode);
            suspensionRepository.save(suspension);
            log.info("Student: {} suspended successfully for subject: {}", studentId, subjectCode);
            return ResponseEntity.ok(new Response("Suspended successfully"));
        } else {
            log.debug("Checking if student: {} is currently suspended for subject: {}", studentId, subjectCode);
            var optional = suspensionRepository.findByStudentIdAndSubjectId(studentId, subjectCode);
            if (optional.isPresent()) {
                suspensionRepository.delete(optional.get());
                log.info("Suspension for student: {} in subject: {} has been restored", studentId, subjectCode);
                return ResponseEntity.status(HttpStatus.OK).body(new Response("Restored"));
            } else {
                log.info("Student: {} is not suspended for subject: {}", studentId, subjectCode);
                return new ResponseEntity<>(new Response("Is not a Suspended Student"),HttpStatus.CONFLICT);
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

    @Transactional
    public ResponseEntity<Response> addStudentToSubject2(String auth,
                                                        String studentId,
                                                        String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>(new Response("Subject not found"), HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        if (cantPerformOperation2(auth, subject)){
            return new ResponseEntity<>(new Response("Unauthorized"),HttpStatus.UNAUTHORIZED);
        }
        Student student = studentService
                .getStudentById(studentId).orElse(null);
        if (student == null) {
            return new ResponseEntity<>(new Response("Student not found"), HttpStatus.NOT_FOUND);
        }
        student.add(subject);
        studentService.saveStudent(student);
        subjectService.save(subject);
        return new ResponseEntity<>(new Response("Student set successfully"), HttpStatus.OK);
    }


    public ResponseEntity<ListOfSubjects> getSubjectList(String auth) {
        log.debug("Extracting token from authorization header");
        String token = jwtService.extractTokenFromHeader(auth);

        log.debug("Extracting user ID from token");
        String id = jwtService.getId(token);

        log.debug("Finding user by ID: {}", id);
        var app = applicationUserService.findUser(id).orElse(null);
        if (app == null) {
            log.warn("User not found with ID: {}", id);
            // You may want to return a suitable error response here
        }

        log.debug("Finding all subjects for lecturer: {}", app);
        Set<Subject> subjects = subjectService.findAllByLecuturerInCharge(app);

        log.debug("Building list of subjects for response");
        ListOfSubjects listOfSubjects = ListOfSubjects.builder()
                .lecturerID(id)
                .lecturerName(app.getLastname() + " " + app.getFirstname())
                .data(subjects.stream().map(subject -> ListOfSubjects.MetaData.builder()
                        .subjectId(subject.getSubjectCode())
                        .subjectTitle(subject.getSubjectTitle())
                        .build()).toList())
                .build();

        log.info("Returning list of subjects for lecturer ID: {}", id);
        return ResponseEntity.ok(listOfSubjects);
    }
}
