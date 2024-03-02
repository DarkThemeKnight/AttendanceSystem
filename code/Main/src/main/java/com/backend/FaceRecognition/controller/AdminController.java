package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authorization_service.admin.AdminService;
import com.backend.FaceRecognition.utils.AddStudentRequest;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller class handling administrative operations.
 * This controller provides endpoints for performing administrative tasks such as managing accounts,
 * subjects, students, and student images.
 * It interacts with the AdminService to execute these operations.
 */
@RestController
@RequestMapping("api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * Constructs an instance of the AdminController with the specified AdminService.
     *
     * @param adminService The service responsible for handling administrative operations.
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Locks the account associated with the given ID.
     *
     * @param id The ID of the account to be locked.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/lock-account")
    public ResponseEntity<String> lockAccount(@RequestParam("id") String id) {
        return adminService.lockAccount(id);
    }

    /**
     * Unlocks the account associated with the given ID.
     *
     * @param id The ID of the account to be unlocked.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/unlock-account")
    public ResponseEntity<String> unlockAccount(@RequestParam("id") String id) {
        return adminService.unlockAccount(id);
    }

    /**
     * Adds a new subject based on the provided request.
     *
     * @param request The request containing the details of the subject to be added.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/add-subject")
    public ResponseEntity<String> addSubject(@RequestBody SubjectRequest request) {
        return adminService.addSubject(request);
    }

    /**
     * Updates an existing subject based on the provided request.
     *
     * @param request The request containing the updated details of the subject.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/update-subject")
    public ResponseEntity<String> updateSubject(@RequestBody SubjectRequest request) {
        return adminService.updateSubject(request);
    }

    /**
     * Deletes an existing subject based on the provided request.
     *
     * @param request The request containing the details of the subject to be deleted.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @DeleteMapping("/delete-subject")
    public ResponseEntity<String> deleteSubject(@RequestBody SubjectRequest request) {
        return adminService.deleteSubject(request);
    }

    /**
     * Retrieves details of a subject identified by its subject code.
     *
     * @param subjectCode The code of the subject to be retrieved.
     * @return A ResponseEntity containing details of the subject if found, otherwise, a not found response.
     */
    @GetMapping("/get-subject/{subjectCode}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable String subjectCode) {
        return adminService.getSubject(subjectCode);
    }

    /**
     * Clears the list of students enrolled in a subject identified by its subject code.
     *
     * @param subjectCode The code of the subject for which the list of students is to be cleared.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/clear-subject-students/{subjectCode}")
    public ResponseEntity<String> clearSubjectStudents(@PathVariable String subjectCode) {
        return adminService.clearSubjectStudents(subjectCode);
    }

    /**
     * Clears all subjects associated with students.
     *
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/clear-all-student-subjects")
    public ResponseEntity<String> clearAllStudentSubjects() {
        return adminService.clearAllStudentSubjects();
    }

    /**
     * Adds a new student based on the provided request.
     *
     * @param request The request containing the details of the student to be added.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/add-student")
    public ResponseEntity<String> addStudent(@RequestBody AddStudentRequest request) {
        return adminService.addStudent(request);
    }

    /**
     * Adds an image of a student identified by their student ID.
     *
     * @param file      The image file to be processed.
     * @param studentId The ID of the student to whom the image belongs.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     */
    @PostMapping("/add-student-image")
    public ResponseEntity<String> addStudentImage(@RequestParam("file") MultipartFile file, @RequestParam("student-id") String studentId) {
        return adminService.addStudentImage(file, studentId);
    }
}
