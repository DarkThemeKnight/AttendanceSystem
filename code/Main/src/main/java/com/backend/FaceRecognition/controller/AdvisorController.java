package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authorization_service.advisor.Advisor;
import com.backend.FaceRecognition.utils.student.StudentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("api/v1/advisor")
public class AdvisorController {

    private final Advisor advisor;

    public AdvisorController(Advisor advisor) {
        this.advisor = advisor;
    }

    /**
     * Endpoint for adding students to a subject.
     * This endpoint allows adding one or more students to a specific subject by providing a list of student details
     * and the subject code.
     * It delegates the operation to the advisor service to handle the addition of students
     * to the subject.
     *
     * @param requestSet   The request containing the details of the students to be added to the subject.
     * @param subjectCode  The code of the subject to which the students are to be added.
     * @return A ResponseEntity indicating the result of the operation.
     *         If the subject with the provided code is not found, a not found response is returned.
     *         If the students are successfully added to the subject, an OK response is returned.
     */
    @PostMapping("/add-students/{subjectCode}")
    public ResponseEntity<String> addStudentsToSubject(@RequestBody StudentRequest requestSet,
                                                       @PathVariable("subjectCode") String subjectCode) {
        return advisor.addStudentToSubject(requestSet, subjectCode);
    }

}
