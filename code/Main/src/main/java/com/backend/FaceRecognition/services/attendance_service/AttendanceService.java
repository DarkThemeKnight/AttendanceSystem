package com.backend.FaceRecognition.services.attendance_service;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.Attendance;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.AttendanceRepository;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import com.backend.FaceRecognition.services.face_recognition_service.FaceRecognitionService;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.AttendanceRecordResponse;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
/**
 * Service class for managing attendance-related operations.
 */
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final SubjectService subjectService;

    public AttendanceService(AttendanceRepository attendanceRepository, FaceRecognitionService faceRecognitionService,
            StudentService studentService, SubjectService subjectService,
            ApplicationUserService applicationUserService) {
        this.attendanceRepository = attendanceRepository;
        this.faceRecognitionService = faceRecognitionService;
        this.subjectService = subjectService;
    }

    /**
     * Initializes attendance records for students enrolled in a subject.
     *
     * @param subjectCode The code of the subject for which attendance is to be
     *                    initialized.
     * @return A ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<String> initializeAttendance(String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        Set<Student> allPossibleAttendees = subject.getStudents();
        LocalDate localDate = LocalDate.now();
        List<Attendance> studentAttendance = allPossibleAttendees.stream()
                .map(student -> new Attendance(student.getMatriculationNumber(), subject.getSubjectCode(), localDate,
                        AttendanceStatus.ABSENT))
                .toList();
        attendanceRepository.saveAll(studentAttendance);
        return new ResponseEntity<>("Initialized Attendance", HttpStatus.OK);
    }

    /**
     * Updates the attendance status for a student in a specific subject based on used
     * face recognition Algorithm.
     *
     * @param subjectCode   The code of the subject for which attendance is being
     *                      updated.
     * @param multipartFile The image file used for face recognition.
     * @param bearer        The authentication token used for authorization.
     * @return A ResponseEntity indicating the result of the attendance update
     *         operation.
     * @throws IOException If an I/O exception occurs while processing the image
     *                     file.
     */
    public ResponseEntity<?> updateAttendanceStatus(String subjectCode, MultipartFile multipartFile, String bearer)
            throws IOException {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>("Subject not found", HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<Student> matriculationNumberResponse = faceRecognitionService.recognizeFace(multipartFile,
                subjectCode, bearer);
        if (matriculationNumberResponse.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)
                || matriculationNumberResponse.getBody() == null) {
            return new ResponseEntity<>("Student not a member of the class", HttpStatus.NOT_FOUND);
        } else if (matriculationNumberResponse.getStatusCode().isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return new ResponseEntity<>("Error when processing file occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Student std = matriculationNumberResponse.getBody();
        Attendance attendance = attendanceRepository.findByStudentIdAndSubjectIdAndDate(std.getMatriculationNumber(),
                subjectCode, LocalDate.now());
        if (attendance == null) {
            return new ResponseEntity<>("Cannot mark attendance anymore", HttpStatus.FORBIDDEN);
        }
        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            return new ResponseEntity<>("Already marked student", HttpStatus.CONFLICT);
        }
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(attendance);
        return new ResponseEntity<>("Successfully marked attendance: student Id = " + std.getMatriculationNumber(),
                HttpStatus.OK);
    }
    /**
     *
     * Retrieves the attendance record for a specific subject and date.
     * This method retrieves the attendance record for a given subject and date from the database
     * and returns it as a JSON response containing information about the subject, date, and
     * attendance status of each student.
     *
     * @param subjectCode The code of the subject for which the attendance record is requested.
     * @param date        The date for which the attendance record is requested.
     * @return A ResponseEntity containing the attendance record as an AttendanceRecordResponse object.
     *         If the subject with the provided code does not exist, a bad request response is returned.
     *         If the attendance record is found, and it has no records, a not found response is returned, else it is returned as part of the response body with a status
     *         of OK (200).
     */
    public ResponseEntity<AttendanceRecordResponse> getRecord(String subjectCode, LocalDate date) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Subject subject = subjectOptional.get();
        List<Attendance> studentAttendance = attendanceRepository.findBySubjectIdAndDate(subjectCode, date);
        if (studentAttendance.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        AttendanceRecordResponse attendanceRecordResponse = new AttendanceRecordResponse();
        attendanceRecordResponse.setTitle(subject.getSubjectTitle());
        attendanceRecordResponse.setSubjectCode(subjectCode);
        attendanceRecordResponse.setDate(date.toString());
        for (Attendance attendance : studentAttendance) {
            attendanceRecordResponse.put(attendance.getStudentId(), attendance.getStatus());
        }
        return ResponseEntity.ok(attendanceRecordResponse);
    }



}
