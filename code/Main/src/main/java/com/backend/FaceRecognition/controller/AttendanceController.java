package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.AttendanceRecord;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;

import com.backend.FaceRecognition.utils.AttendanceRecordResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Initializes attendance records for a specified subject.
     * This endpoint initializes attendance records for students enrolled in the specified subject.
     * It delegates the initialization process to the AttendanceService.
     *
     * @param subjectCode The code of the subject for which attendance is to be initialized.
     * @return A ResponseEntity indicating the result of the initialization operation.
     *         If the subject is not found or if there are errors during initialization,
     *         an appropriate error response is returned. Otherwise, a success response is returned.
     */
    @PostMapping("/initialize/{subject_code}")
    public ResponseEntity<String> initializeAttendance(@PathVariable("subject_code") String subjectCode) {
        // Delegate the initialization process to the AttendanceService
        return attendanceService.initializeAttendance(subjectCode);
    }

    /**
     * Updates the attendance status based on face recognition for a specified subject.
     * This endpoint updates the attendance status for a student in the specified subject
     * based on face recognition. It takes a multipart file containing the image of the student
     * and an authorization token from the request headers.
     *
     * @param subjectCode The code of the subject for which attendance is being updated.
     * @param file        The multipart file containing the image used for face recognition.
     * @param request     The HTTP servlet request containing the authorization token.
     * @return A ResponseEntity indicating the result of the attendance update operation.
     *         If the subject is not found, the student is not recognized, or if there are errors
     *         during the update process, an appropriate error response is returned.
     *         Otherwise, a success response is returned.
     */
    @PostMapping("/update/{subject_code}")
    public ResponseEntity<?> updateAttendanceStatus(@PathVariable("subject_code") String subjectCode,
                                                    @RequestParam MultipartFile file, HttpServletRequest request){
        // Delegate the attendance status update process to the AttendanceService
        try {
            return attendanceService.updateAttendanceStatus(subjectCode, file, request.getHeader("Authorization"));
        }catch (IOException e){
            return new ResponseEntity<>("Bad File", HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * Retrieves the attendance record for a specified subject and date.
     * This endpoint retrieves the attendance record for the specified subject and date.
     * It takes the subject code and date as parameters and delegates the retrieval process
     * to the AttendanceService.
     *
     * @param subjectCode The code of the subject for which the attendance record is requested.
     * @param date        The date for which the attendance record is requested (formatted as "yyyy-MM-dd").
     * @return A ResponseEntity containing the attendance record as an AttendanceRecordResponse object.
     *         If the subject is not found, or if there are no attendance records for the specified date,
     *         an appropriate error response is returned. Otherwise, the attendance record is returned
     *         as part of the response body with a status of OK (200).
     */
    @GetMapping("/record/{subject_code}")
    public ResponseEntity<AttendanceRecordResponse> getRecord(@PathVariable("subject_code") String subjectCode,
                                                              @RequestParam String date) {
        // Parse the date string into a LocalDate object
        LocalDate localDate = LocalDate.parse(date);

        // Delegate the retrieval process to the AttendanceService
        return attendanceService.getRecord(subjectCode, localDate);
    }

}
