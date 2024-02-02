package com.unilorin.attendance_system.datapersistence_api.service;

import com.unilorin.attendance_system.datapersistence_api.entity.Student;
import com.unilorin.attendance_system.datapersistence_api.entity.Subject;
import com.unilorin.attendance_system.datapersistence_api.repo.ApplicationUserRepository;
import com.unilorin.attendance_system.datapersistence_api.repo.StudentRepository;
import com.unilorin.attendance_system.datapersistence_api.repo.SubjectRepository;
import com.unilorin.attendance_system.datapersistence_api.utils.StudentRequest;
import com.unilorin.attendance_system.datapersistence_api.utils.StudentResponse;
import com.unilorin.attendance_system.datapersistence_api.utils.SubjectResponseDto;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class StudentService {
    private final ApplicationUserRepository repository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    @Autowired
    public StudentService(ApplicationUserRepository repository, StudentRepository studentRepository, SubjectRepository subjectRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }
    public ResponseEntity<String> addSubject(String studentId, Set<String> subjects){
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()){
            return new ResponseEntity<>("Student not found",HttpStatus.NOT_FOUND);
        }
        Set<Subject> subject = subjects.stream()
                .map(subjectRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        if (subject.isEmpty()){
            return new ResponseEntity<>("All subjects are invalid",HttpStatus.NOT_FOUND);
        }
        Student student1 = student.get();
        student1.add(subject);
        studentRepository.save(student1);
        return new ResponseEntity<>("Updated Successfully",HttpStatus.OK);
    }
    @Transactional
    public ResponseEntity<String> addStudent(StudentRequest request){
        log.info("adding student {}",request.getMatriculationNumber());
        Student student = findStudent(request.getMatriculationNumber());
        if (student != null){
            log.info("already added {}",request.getMatriculationNumber());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        student = parse(request);
        student = studentRepository.save(student);
        Set<Subject> subjects = request.getSubjects().stream()
                .map(subjectRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        student.setSubjects(subjects);
        studentRepository.save(student);
        log.info("Successfully added {}",request.getMatriculationNumber());
        return new ResponseEntity<>("Successfully added "+request.getMatriculationNumber(),HttpStatus.OK);
    }
    public ResponseEntity<String> addAll(List<StudentRequest> requests){
        List<ResponseEntity<String>> save = requests.stream()
                .map(this::addStudent)
                .toList();
        String s = save.stream().map(stringResponseEntity -> stringResponseEntity.getBody()+" status: "+stringResponseEntity.getStatusCode()+"\n").collect(Collectors.joining());
        return new ResponseEntity<>(s,HttpStatus.OK);
    }
    private Student parse(StudentRequest request){
        Student student = new Student();
        student.setFirstname(request.getFirstname());
        student.setLastname(request.getLastname());
        student.setMiddleName(request.getMiddleName());
        student.setMatriculationNumber(request.getMatriculationNumber());
        student.setFaceImage(request.getFaceImage());
        student.setSchoolEmail(request.getSchoolEmail());
        return student;
    }
    private StudentResponse parse(Student student){
        StudentResponse response = new StudentResponse();
        response.setId(student.getMatriculationNumber());
        response.setImage(student.getFaceImage());
        Set<SubjectResponseDto> subjectResponseDtos = student.getSubjects().stream()
                .map(subject -> {
                    SubjectResponseDto responseDto = new SubjectResponseDto();
                    responseDto.setSubjectTitle(subject.getSubjectTitle());
                    responseDto.setSubjectCode(subject.getSubjectCode());
                    return responseDto;
                })
                .collect(Collectors.toSet());
        response.setSubject(subjectResponseDtos);
        return response;
    }
    private Student findStudent(String id){
        return studentRepository.findById(id).orElse(null);
    }
    public ResponseEntity<StudentResponse> getStudent(String id){
        Optional<Student> student = studentRepository.findById(id);
        return student.map(value -> new ResponseEntity<>(parse(value), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(new StudentResponse(), HttpStatus.NOT_FOUND));
    }



}
