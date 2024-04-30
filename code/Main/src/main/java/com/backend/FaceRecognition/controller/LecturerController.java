package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.attendance_service.AttendanceService;

import com.backend.FaceRecognition.services.authorization_service.lecturer_service.LecturerService;
import com.backend.FaceRecognition.utils.AttendanceRecordResponse;
import com.backend.FaceRecognition.utils.AvailableRecords;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import com.backend.FaceRecognition.utils.student.StudentRequest;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("api/v1/attendance")
public class LecturerController {
    private final AttendanceService attendanceService;
    private final LecturerService lecturerService;

    public LecturerController(AttendanceService attendanceService, LecturerService lecturerService) {
        this.attendanceService = attendanceService;
        this.lecturerService = lecturerService;
    }
    @PostMapping("/initialize")
    public ResponseEntity<Response> initializeAttendance(
            @RequestParam String subjectCode,
            @RequestParam int duration,
            HttpServletRequest request) {
        var r = attendanceService.initializeAttendance(subjectCode,
                request.getHeader("Authorization"), duration);
        return new ResponseEntity<>(new Response(r.getBody()), r.getStatusCode());
    }
    @PostMapping("/update/{subject_code}")
    public ResponseEntity<Response> updateAttendanceStatus(@PathVariable("subject_code") String subjectCode,
            @RequestParam MultipartFile file,
            HttpServletRequest request) {
        // Delegate the attendance status update process to the AttendanceService
        ResponseEntity<String> response = attendanceService.updateAttendanceStatus(subjectCode, file,
                request.getHeader("Authorization"));
        return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());
    }
    @GetMapping("/record")
    public ResponseEntity<AttendanceRecordResponse> getRecord(@RequestParam String subjectCode,
            @RequestParam String date,
            @RequestParam("sort_id") int id,
            @RequestHeader("Authorization") String bearer) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            return attendanceService.getRecord(subjectCode, localDate, id, bearer);
        }catch (DateTimeParseException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/available-records")
    public ResponseEntity<AvailableRecords> getAvailableRecord(@RequestParam String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return attendanceService.getRecord(subjectCode, bearer);
    }

    @PostMapping("/clear")
    public ResponseEntity<Response> clearSubjectStudents(@RequestParam String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return build(lecturerService.clearSubjectStudents(subjectCode, bearer));
    }
    @PostMapping("/add")
    public ResponseEntity<Response> addStudentToSubject(
            @RequestBody StudentRequest requestSet,
            @RequestParam String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return lecturerService.addStudentToSubject(bearer, requestSet, subjectCode);
    }
    private ResponseEntity<Response> build(ResponseEntity<String> response) {
        return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());
    }

    @PostMapping("/suspend")
    public ResponseEntity<Response> suspendStudentFromMarkingAttendance(
            @RequestParam String subjectCode,
            @RequestParam String studentId,
            @RequestHeader("Authorization") String bearer) {
        return lecturerService.suspendStudentFromMarkingAttendance(bearer, subjectCode, studentId);
    }
    @GetMapping("/student-record")
    private ResponseEntity<StudentAttendanceRecordResponse> viewAttendanceRecord(
            @RequestHeader("Authorization") String bearer,
            @RequestParam String studentId,
            String subjectCode) {
        return lecturerService.viewAttendanceRecord(bearer, studentId, subjectCode);
    }
    @GetMapping("/print")
    public ResponseEntity<ByteArrayResource> getRecordExcelSheet(@RequestParam String subjectCode,
            @RequestParam String date,
            @RequestParam("sort_id") int id,
            @RequestHeader("Authorization") String bearer) {
        LocalDate localDate = LocalDate.parse(date);
        return attendanceService.getAttendanceExcel(subjectCode, localDate, id, bearer);
    }

}