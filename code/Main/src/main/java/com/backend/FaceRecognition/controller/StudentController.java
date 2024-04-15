package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    @Autowired
    public StudentController(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }
    @GetMapping("/print")
    public ResponseEntity<ByteArrayResource> printAttendanceRecord(@RequestHeader("Authorization") String bearer) {
        return attendanceService.printAttendanceRecord(bearer);
    }

//    @GetMapping("/")
//    public List<Student> getAllStudents() {
//        return studentService.getAllStudents();
//    }
//
    @GetMapping("/{matriculationNumber}")
    public ResponseEntity<Student> getStudentById(@PathVariable String matriculationNumber) {
        Optional<Student> student = studentService.getStudentById(matriculationNumber);
        return student.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
//
//    @PostMapping("/")
//    public ResponseEntity<Response> saveStudent(@RequestBody Student student) {
//        studentService.saveStudent(student);
//        return ResponseEntity.ok(new Response("Student saved successfully"));
//    }
//
//    @DeleteMapping("/{matriculationNumber}")
//    public ResponseEntity<Response> deleteStudentById(@PathVariable String matriculationNumber) {
//        studentService.deleteStudentById(matriculationNumber);
//        return ResponseEntity.ok(new Response("Student deleted successfully"));
//    }

    @PostMapping("/image")
    public ResponseEntity<Response> addStudentImage(@RequestParam("file") MultipartFile file,
                                                    @RequestHeader("Authorization") String auth) {
        ResponseEntity<String> response = studentService.addStudentImage(file, auth);
        return build(response);
    }
//    @GetMapping("/course/{subjectCode}")
//    public Set<Student> getAllStudentsOfferingCourse(@PathVariable String subjectCode) {
//        return studentService.getAllStudentsOfferingCourse(subjectCode);
//    }
    private ResponseEntity<Response> build(ResponseEntity<String> initial){
        return new ResponseEntity<>(new Response(initial.getBody()),initial.getStatusCode());
    }
    @GetMapping("/view-attendance-record")
    private ResponseEntity<StudentAttendanceRecordResponse> getAttendance(@RequestHeader("Authorization") String bearer){
        return attendanceService.viewAttendanceRecord(bearer);
    }
}
