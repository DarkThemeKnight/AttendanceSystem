package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.services.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{matriculationNumber}")
    public ResponseEntity<Student> getStudentById(@PathVariable String matriculationNumber) {
        Optional<Student> studentOptional = studentService.getStudentById(matriculationNumber);
        return studentOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        Student savedStudent = studentService.saveStudent(student);
        return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
    }

    @DeleteMapping("/{matriculationNumber}")
    public ResponseEntity<Void> deleteStudentById(@PathVariable String matriculationNumber) {
        studentService.deleteStudentById(matriculationNumber);
        return ResponseEntity.noContent().build();
    }
}
