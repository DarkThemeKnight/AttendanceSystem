package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.entities.Notification;
import com.backend.FaceRecognition.repository.NotificationRepository;
import com.backend.FaceRecognition.services.extras.ScheduleService;
import com.backend.FaceRecognition.utils.NotificationResponse;
import com.backend.FaceRecognition.utils.ScheduleSetupRequest;
import com.backend.FaceRecognition.utils.ScheduleSetupResponse;
import com.backend.FaceRecognition.utils.StudentProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/general")
public class General {
    private final StudentService studentService;
    private final NotificationRepository notificationRepository;
    private final ScheduleService scheduleService;
    private final JwtService jwtService;
    private final ApplicationUserService applicationUserService;

    public General(StudentService studentService, NotificationRepository notificationRepository, ScheduleService scheduleService,
                   JwtService jwtService, ApplicationUserService applicationUserService) {
        this.studentService = studentService;
        this.notificationRepository = notificationRepository;
        this.scheduleService = scheduleService;
        this.applicationUserService = applicationUserService;
        this.jwtService = jwtService;
    }
    @GetMapping("/notification")
    public ResponseEntity<NotificationResponse> getMyNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        List<NotificationResponse.CustomData> data = notifications.stream()
                .map(notification -> new NotificationResponse.CustomData(notification.getTitle(),
                        notification.getContent()))
                .toList();
        NotificationResponse response = new NotificationResponse("successful", data);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/schedule")
    public ResponseEntity<ScheduleSetupResponse> getMySchedule(@RequestHeader("Authorization") String bearer) {
        return scheduleService.fetch(bearer);
    }
    @GetMapping("/schedule/{day}")
    public ResponseEntity<ScheduleSetupResponse> getMySchedule(@RequestHeader("Authorization") String bearer, @PathVariable("day") String day) {
        return scheduleService.fetch(bearer, day);
    }
    @PostMapping("/schedule")
    public ResponseEntity<ScheduleSetupResponse> addSchedule(@RequestBody ScheduleSetupRequest scheduleRequest,
            @RequestHeader("Authorization") String auth) {
        return scheduleService.setupMySchedule(scheduleRequest, auth);
    }
    @GetMapping
    public ResponseEntity<ApplicationUser> whoAmI(@RequestHeader("Authorization") String bearer) {
        String userId = jwtService.getId(jwtService.extractTokenFromHeader(bearer));
        return applicationUserService.findUser(userId).stream().findFirst().map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/studentProfile")
    public ResponseEntity<StudentProfile> getProfile(@RequestParam String studentId){
        return studentService.getMyProfile(studentId);
    }


}
