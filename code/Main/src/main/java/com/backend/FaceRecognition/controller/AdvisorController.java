    package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authorization_service.advisor.Advisor;
import com.backend.FaceRecognition.utils.student.StudentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/advisor")
public class AdvisorController {

    private final Advisor advisor;

    @Autowired
    public AdvisorController(Advisor advisor) {
        this.advisor = advisor;
    }

    @PostMapping("/add-students/{subjectCode}")
    public ResponseEntity<String> addStudentsToSubject(@RequestBody Set<StudentRequest> requestSet,
                                                       @PathVariable String subjectCode) {
        return advisor.addStudentsToSubject(requestSet, subjectCode);
    }
}
