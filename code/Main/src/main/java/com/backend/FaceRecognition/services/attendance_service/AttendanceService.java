package com.backend.FaceRecognition.services.attendance_service;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.Attendance;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.AttendanceRepository;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import com.backend.FaceRecognition.services.face_recognition_service.FaceRecognitionService;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
@Slf4j
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ApplicationUserService applicationUserService;
    public AttendanceService(AttendanceRepository attendanceRepository, FaceRecognitionService faceRecognitionService, StudentService studentService, SubjectService subjectService, ApplicationUserService applicationUserService) {
        this.attendanceRepository = attendanceRepository;
        this.faceRecognitionService = faceRecognitionService;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.applicationUserService = applicationUserService;
    }
    public ResponseEntity<String> initializeAttendance(String subjectCode) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        Set<Student> allPossibleAttendees = subject.getStudents();
        LocalDate localDate = LocalDate.now();
        List<Attendance> studentAttendance = allPossibleAttendees.stream()
                .map(student -> new Attendance(student.getMatriculationNumber(), subject.getSubjectCode(), localDate, AttendanceStatus.ABSENT))
                .toList();
        attendanceRepository.saveAll(studentAttendance);//save attendance
        return new ResponseEntity<>("Initialized Attendance", HttpStatus.OK);
    }
    public ResponseEntity<String> updateAttendanceStatus(String subjectCode, MultipartFile multipartFile){
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()){
            return new ResponseEntity<>("Subject not found",HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        Student found = null;
        try {
            found = faceRecognitionService.recognizeFace(multipartFile, subject);
        }catch (IOException e){
            return new ResponseEntity<>("BAD IMAGE",HttpStatus.BAD_REQUEST);
        }catch (NullPointerException e){
            return new ResponseEntity<>("NO IMAGE",HttpStatus.BAD_REQUEST);
        }
        if (found != null) {
            if (found.getSubjects().contains(subject)) {
                Attendance attendance = attendanceRepository.findByStudentIdAndSubjectIdAndDate(found.getMatriculationNumber(), subject.getSubjectCode(), LocalDate.now());
                attendance.setStatus(AttendanceStatus.PRESENT);
                attendanceRepository.save(attendance);
                return new ResponseEntity<>("Accepted: " + found.getMatriculationNumber(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Student Not part of this class", HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
    }
    public ResponseEntity<byte[]> getRecord(String subjectCode, LocalDate date) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Subject subject = subjectOptional.get();
        List<Attendance> studentAttendance = attendanceRepository.findBySubjectIdAndDate(subjectCode, date);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.write("Title: " + subject.getSubjectTitle() + "\n");
            writer.write("Code: " + subjectCode + "\n");
            writer.write("Date: " + date.toString() + "\n");
            writer.write("Student Matriculation Number, Status\n");
            for (Attendance attendance : studentAttendance) {
                writer.write(attendance.getStudentId() + "," + attendance.getStatus() + "\n");
            }
            writer.flush();
            writer.close();
            byte[] fileContent = outputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("filename", "attendance_record.csv");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (IOException e) {
            log.error("Internal server error could not read file \n",e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
