package com.backend.FaceRecognition.services.image_request_service;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.utils.image.ImageIO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class ImageRequestService {
    private final StudentService studentService;

    public ImageRequestService(StudentService studentService) {
        this.studentService = studentService;
    }
    public List<ImageIO> getAllImages(){
        List<Student> students = studentService.getAllStudents();
        return students.stream().map(this::parse).filter(Objects::nonNull).flatMap(List::stream).toList();
    }

    public List<ImageIO> parse(Student student){
        List<byte[]> images = student.getFaceImages();
        if (images == null){
            return null;
        }
        return images.stream().map(bytes -> new ImageIO(student.getMatriculationNumber(),bytes)).toList();
    }

    public List<ImageIO> getAllFromFaculty(String faculty){
        List<Student> students = studentService.sortByFaculty(faculty);
        return students.stream().map(this::parse).filter(Objects::nonNull).flatMap(Collection::stream).toList();
    }
    public List<ImageIO> getAllFromDepartment(String department){
        List<Student> students = studentService.sortByFaculty(department);
        return students.stream().map(this::parse).filter(Objects::nonNull).flatMap(Collection::stream).toList();
    }
    public List<ImageIO> getStudentImage(String matriculation_number){
        Student student = studentService.getStudentById(matriculation_number).orElse(null);
        if (student == null){
            return null;
        }
        return parse(student);
    }

}
