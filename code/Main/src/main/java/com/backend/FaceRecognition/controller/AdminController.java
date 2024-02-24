package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authorization_service.admin.AdminService;
import com.backend.FaceRecognition.utils.AddStudentRequest;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/lock-account")
    public ResponseEntity<String> lockAccount(@RequestParam("id") String id) {
        return adminService.lockAccount(id);
    }

    @PostMapping("/unlock-account")
    public ResponseEntity<String> unlockAccount(@RequestParam("id") String id) {
        return adminService.unlockAccount(id);
    }

    @PostMapping("/add-subject")
    public ResponseEntity<String> addSubject(@RequestBody Set<SubjectRequest> subjectSet) {
        return adminService.addSubject(subjectSet);
    }

    @PostMapping("/add-subject/single")
    public ResponseEntity<String> addSubject(@RequestBody SubjectRequest request) {
        return adminService.addSubject(request);
    }

    @PostMapping("/update-subject")
    public ResponseEntity<String> updateSubject(@RequestBody SubjectRequest request) {
        return adminService.updateSubject(request);
    }

    @DeleteMapping("/delete-subject")
    public ResponseEntity<String> deleteSubject(@RequestBody SubjectRequest request) {
        return adminService.deleteSubject(request);
    }

    @GetMapping("/get-subject/{subjectCode}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable String subjectCode) {
        return adminService.getSubject(subjectCode);
    }

    @PostMapping("/clear-subject-students/{subjectCode}")
    public ResponseEntity<String> clearSubjectStudents(@PathVariable String subjectCode) {
        return adminService.clearSubjectStudents(subjectCode);
    }

    @PostMapping("/clear-all-student-subjects")
    public ResponseEntity<String> clearAllStudentSubjects() {
        return adminService.clearAllStudentSubjects();
    }

    @PostMapping("/add-student")
    public ResponseEntity<String> addStudent(@RequestBody AddStudentRequest request) {
        return adminService.addStudent(request);
    }

    @PostMapping("/add-student-image")
    public ResponseEntity<String> addStudentImage(@RequestParam("file") MultipartFile file, @RequestParam("student-id") String studentId) {
        return adminService.addStudentImage(file, studentId);
    }
}
