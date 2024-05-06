package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin("*")
public class StudentController {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    public StudentController(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }
    @GetMapping("/print")
    public ResponseEntity<ByteArrayResource> printAttendanceRecord(@RequestHeader("Authorization") String bearer, @RequestParam String subjectCode) {
        return attendanceService.printAttendanceRecord(bearer,subjectCode);
    }
    @PostMapping("/image")
    public ResponseEntity<Response> addStudentImage(@RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String auth) {
        ResponseEntity<String> response = studentService.addStudentImage(file, auth);
        return build(response);
    }
    private ResponseEntity<Response> build(ResponseEntity<String> initial) {
        return new ResponseEntity<>(new Response(initial.getBody()), initial.getStatusCode());
    }
    @GetMapping("/view")
    private ResponseEntity<StudentAttendanceRecordResponse> getAttendance(
            @RequestHeader("Authorization") String bearer, @RequestParam String subjectCode) {
        return attendanceService.viewAttendanceRecord(bearer,subjectCode);
    }
}
