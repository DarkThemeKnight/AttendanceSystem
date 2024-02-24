package com.backend.FaceRecognition.services.face_recognition_service;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@Slf4j
public class FaceRecognitionService {
    private final StudentService studentService;
    private final SubjectService subjectService;
    @Autowired
    public FaceRecognitionService(StudentService studentService, SubjectService subjectService) {
        this.studentService = studentService;
        this.subjectService = subjectService;
    }
    public Student recognizeFace(MultipartFile multipartFile, Subject subject) throws IOException,NullPointerException{
        String UPLOAD_URL = "http:/localhost:8081/recognize";
        return null;
    }
}
