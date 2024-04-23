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

@RestController
@RequestMapping("api/v1/attendance")
public class LecturerController {
    private final AttendanceService attendanceService;
    private final LecturerService lecturerService;

    public LecturerController(AttendanceService attendanceService, LecturerService lecturerService) {
        this.attendanceService = attendanceService;
        this.lecturerService = lecturerService;
    }

    @PostMapping("/initialize/{subject_code}")
    public ResponseEntity<Response> initializeAttendance(@PathVariable("subject_code") String subjectCode,
            @RequestParam("duration") int duration,
            HttpServletRequest request) {
        // Delegate the initialization process to the AttendanceService
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

    @GetMapping("/record/{subject_code}")
    public ResponseEntity<AttendanceRecordResponse> getRecord(@PathVariable("subject_code") String subjectCode,
            @RequestParam("date") String date,
            @RequestParam("sort_id") int id,
            @RequestHeader("Authorization") String bearer) {
        LocalDate localDate = LocalDate.parse(date);
        return attendanceService.getRecord(subjectCode, localDate, id, bearer);
    }

    @GetMapping("/available-records{subject_code}")
    public ResponseEntity<AvailableRecords> getAvailableRecord(@PathVariable("subject_code") String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return attendanceService.getRecord(subjectCode, bearer);
    }

    @PostMapping("/clear-subject-students")
    public ResponseEntity<Response> clearSubjectStudents(@RequestParam("subject_code") String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return build(lecturerService.clearSubjectStudents(subjectCode, bearer));
    }

    @PutMapping("/add-student-to-subject")
    public ResponseEntity<Response> addStudentToSubject(@RequestBody StudentRequest requestSet,
            @RequestParam("subject_code") String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return lecturerService.addStudentToSubject(bearer, requestSet, subjectCode);
    }

    private ResponseEntity<Response> build(ResponseEntity<String> response) {
        return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());
    }

    @PostMapping("/suspend")
    public ResponseEntity<Response> suspendStudentFromMarkingAttendance(
            @RequestParam("subject_code") String subjectCode,
            @RequestParam("student_id") String studentId,
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

    @GetMapping("/print-record/{subject_code}")
    public ResponseEntity<ByteArrayResource> getRecordExcelSheet(@PathVariable("subject_code") String subjectCode,
            @RequestParam("date") String date,
            @RequestParam("sort_id") int id,
            @RequestHeader("Authorization") String bearer) {
        LocalDate localDate = LocalDate.parse(date);
        return attendanceService.getAttendanceExcel(subjectCode, localDate, id, bearer);
    }

}