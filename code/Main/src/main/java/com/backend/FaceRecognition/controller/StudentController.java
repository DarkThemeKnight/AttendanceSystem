package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin("*")
@Component
@RequiredArgsConstructor
@Slf4j
public class StudentController {
    private final StudentService studentService;
    private final AttendanceService attendanceService;

    @PostMapping("/update")
    public ResponseEntity<Response> updateAttendanceStatus(
            @RequestParam String attendanceCode,
            @RequestParam("image") MultipartFile file) throws IOException {
        attendanceCode = attendanceCode.replace(" ", "");
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return "x" + new Random().nextLong(9000000000L) + ".jpg";
            }
        };
        // Log the attendance code
        log.info("Updating attendance code => {}", attendanceCode);
        // Call the attendance service to update the status
        ResponseEntity<String> response = attendanceService.updateAttendanceStatus(attendanceCode, resource);
        // Log the response status and body
        log.info("{}, code => {}", response.getBody(), response.getStatusCode());
        // Return the response
        return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());
    }


    @GetMapping("/print")
    public ResponseEntity<ByteArrayResource> printAttendanceRecord(@RequestHeader("Authorization") String bearer) {
        return attendanceService.printAttendanceRecord(bearer);
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
