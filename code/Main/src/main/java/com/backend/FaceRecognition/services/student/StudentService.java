package com.backend.FaceRecognition.services.student;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    public Optional<Student> getStudentById(String matriculationNumber) {
        return studentRepository.findById(matriculationNumber);
    }
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }
    public void saveAll(Collection<Student> student) {
         studentRepository.saveAll(student);
    }
    public void deleteStudentById(String matriculationNumber) {
        studentRepository.deleteById(matriculationNumber);
    }

}
