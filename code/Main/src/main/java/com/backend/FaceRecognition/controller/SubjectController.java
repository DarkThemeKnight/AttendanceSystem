package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.subject.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/{subjectCode}")
    public ResponseEntity<Subject> getSubjectByCode(@PathVariable String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        return subjectOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> addSubject(@RequestBody Subject subject) {
        subjectService.saveSubject(subject);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{subjectCode}")
    public ResponseEntity<Void> deleteSubjectByCode(@PathVariable String subjectCode) {
        subjectService.deleteSubjectByCode(subjectCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        List<Subject> subjects = subjectService.findAllSubjects();
        return ResponseEntity.ok(subjects);
    }

    @PostMapping("/addAll")
    public ResponseEntity<Void> addAllSubjects(@RequestBody Set<Subject> subjects) {
        subjectService.addAll(subjects);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
