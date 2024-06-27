package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin("*")
@Component
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final AttendanceService attendanceService;

    @PostMapping("/update")
    public ResponseEntity<Response> updateAttendanceStatus(
            @RequestParam String attendanceCode,
            @RequestParam(name="image") MultipartFile file) {
        ResponseEntity<String> response = attendanceService.updateAttendanceStatus(attendanceCode,
                file);
        return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());
    }
    @GetMapping("/print")
    public ResponseEntity<ByteArrayResource> printAttendanceRecord(@RequestHeader("Authorization") String bearer,
                                                                   @RequestParam String subjectCode) {
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
