package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/hardware")
public class HardwareController {
    private final AttendanceService attendanceService;
    public HardwareController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    @PostMapping("/update")
    public ResponseEntity<Response> updateAttendanceStatus(
            @RequestParam String attendanceCode,
            @RequestParam MultipartFile file,
            HttpServletRequest request) {
        ResponseEntity<String> response = attendanceService.updateAttendanceStatus(attendanceCode,
                file, request.getHeader("Authorization"));
            return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());


    }
    @GetMapping("/")
    public ResponseEntity<Response> ping(){
  	return ResponseEntity.ok(new Response("Accepted"));
    }


}
