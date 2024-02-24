package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeAttendance(@RequestParam String subjectCode) {
        return attendanceService.initializeAttendance(subjectCode);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateAttendanceStatus(@RequestParam String subjectCode, @RequestParam MultipartFile file) {
        return attendanceService.updateAttendanceStatus(subjectCode, file);
    }

    @GetMapping("/record")
    public ResponseEntity<byte[]> getRecord(@RequestParam String subjectCode, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return attendanceService.getRecord(subjectCode, localDate);
    }
}
