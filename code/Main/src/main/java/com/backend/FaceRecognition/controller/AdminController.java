package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Notification;
import com.backend.FaceRecognition.repository.NotificationRepository;
import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.services.authorization_service.admin.AdminService;
import com.backend.FaceRecognition.utils.NotificationRequest;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/admin")
public class AdminController {
    private final AdminService adminService;
    private final AuthenticationService authenticationService;
    private final NotificationRepository notificationRepository;
    public AdminController(AdminService adminService, AuthenticationService authenticationService, NotificationRepository notificationRepository) {
        this.adminService = adminService;
        this.authenticationService = authenticationService;
        this.notificationRepository = notificationRepository;
    }
    @PostMapping("/setup_notification")
    public ResponseEntity<Response> notification(@RequestBody NotificationRequest request){
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setValidUntil(request.getValidUntil());
        notificationRepository.save(notification);
        return ResponseEntity.ok(new Response("Notification Saved"));
    }
    @PutMapping("/update-subject")
    public ResponseEntity<Response> updateSubject(@RequestBody SubjectRequest request) {
        return build(adminService.updateSubject(request));
    }
    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody ApplicationUserRequest
                                                         applicationUser, @RequestParam String type) {
        return authenticationService.register(applicationUser,type.toLowerCase());
    }
    @PostMapping("/lock-account")
    public ResponseEntity<Response> lockAccount(@RequestParam("id") String id, @RequestHeader("Authorization") String bearer) {
        return build(adminService.lockAccount(id,bearer));
    }
    @PostMapping("/unlock-account")
    public ResponseEntity<Response> unlockAccount(@RequestParam("id") String id,@RequestHeader("Authorization") String bearer) {
        return build(adminService.unlockAccount(id,bearer));
    }
    @DeleteMapping("/delete-subject")
    public ResponseEntity<Response> deleteSubject(@RequestParam("id") String subjectId) {
        return build(adminService.deleteSubject(subjectId));
    }
    private ResponseEntity<Response> build(ResponseEntity<String> response){
        return new ResponseEntity<>(new Response(response.getBody()),response.getStatusCode());
    }

    @GetMapping("/get-subject/{subjectCode}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable String subjectCode) {
        return adminService.getSubject(subjectCode);
    }

    @PostMapping("/clear-all-student-subjects")
    public ResponseEntity<Response> clearAllStudentSubjects() {
        return build(adminService.clearAllStudentSubjects());
    }

    @PostMapping("/add-subject")
    public ResponseEntity<Response> addSubject(@RequestBody SubjectRequest request) {
        return build(adminService.addSubject(request));
    }

}
