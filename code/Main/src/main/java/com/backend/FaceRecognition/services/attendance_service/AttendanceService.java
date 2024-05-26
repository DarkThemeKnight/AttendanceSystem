package com.backend.FaceRecognition.services.attendance_service;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.*;
import com.backend.FaceRecognition.repository.AttendanceRepository;
import com.backend.FaceRecognition.repository.AttendanceSetupPolicyRepository;
import com.backend.FaceRecognition.repository.SuspensionRepository;
import com.backend.FaceRecognition.services.face_recognition_service.FaceRecognitionService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.AttendanceRecordResponse;
import com.backend.FaceRecognition.utils.AvailableRecords;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import com.backend.FaceRecognition.utils.UniqueCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing attendance-related operations.
 */
@Service
@Slf4j
public class AttendanceService {
    private final AttendanceSetupPolicyRepository attendanceSetupRepository;
    private final AttendanceRepository attendanceRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final SubjectService subjectService;
    private final JwtService jwtService;
    private final StudentService studentService;
    private final SuspensionRepository suspensionRepository;

    public AttendanceService(AttendanceSetupPolicyRepository attendanceSetupRepository,
            AttendanceRepository attendanceRepository, FaceRecognitionService faceRecognitionService,
            SubjectService subjectService, JwtService jwtService, StudentService studentService,
            SuspensionRepository suspensionRepository) {
        this.attendanceSetupRepository = attendanceSetupRepository;
        this.attendanceRepository = attendanceRepository;
        this.faceRecognitionService = faceRecognitionService;
        this.subjectService = subjectService;
        this.jwtService = jwtService;
        this.studentService = studentService;
        this.suspensionRepository = suspensionRepository;
    }

    public ResponseEntity<String> initializeAttendance(String subjectCode, String authorization, int duration) {
        List<Attendance> attendances = attendanceRepository.findBySubjectIdAndDate(subjectCode, LocalDate.now());
        if (!attendances.isEmpty()) {
            return ResponseEntity.badRequest().body("attendance already initialized");
        }
        if (duration < 10) {
            return ResponseEntity.badRequest().body("Duration at least 10 minutes");
        }
        String jwtToken = jwtService.extractTokenFromHeader(authorization);
        String id = jwtService.getId(jwtToken);
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            return new ResponseEntity<>("Unauthorized to take attendance", HttpStatus.UNAUTHORIZED);
        }
        Set<Student> allPossibleAttendees = studentService.getAllStudentsOfferingCourse(subjectCode);
        LocalDate localDate = LocalDate.now();
        List<Attendance> studentAttendance = allPossibleAttendees.stream()
                .map(student -> new Attendance(student.getMatriculationNumber(),
                        subject.getSubjectCode(),
                        localDate,
                        AttendanceStatus.ABSENT))
                .toList();
        AttendanceSetupPolicy setup = AttendanceSetupPolicy.builder()
                .code(UniqueCodeGenerator.generateCode(10))
                .attendanceDateTime(LocalDateTime.now())
                .duration(duration)
                .subjectId(subjectCode)
                .attendanceDate(localDate)
                .attendanceDateTime(LocalDateTime.now().plusMinutes(duration))
                .build();
        setup = attendanceSetupRepository.save(setup);
        attendanceRepository.saveAll(studentAttendance);
        return new ResponseEntity<>("code="+setup.getCode(), HttpStatus.OK);
    }
    public ResponseEntity<String> updateAttendanceStatus(String attendanceCode,
                                                         MultipartFile multipartFile,
                                                            String bearer) {
        Optional<AttendanceSetupPolicy> attendanceSetup =
                attendanceSetupRepository.findById(attendanceCode);
        if (attendanceSetup.isEmpty()) {
            return ResponseEntity.badRequest().body("Attendance is not initialized yet");
        }
        AttendanceSetupPolicy policy = attendanceSetup.get();
        if (LocalDateTime.now().isAfter(policy.getAttendanceDateTime()
                .plusMinutes(policy.getDuration()))) {
            return ResponseEntity.badRequest().body("Time Expired");
        }
        String subjectCode = policy.getSubjectId();
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>("Subject not found", HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<Student> matriculationNumberResponse;
        try {
            matriculationNumberResponse = faceRecognitionService.recognizeFace(multipartFile,
                    subjectCode, bearer);
        }catch (HttpClientErrorException ex){
            return new ResponseEntity<>(ex.getResponseBodyAsString(), HttpStatusCode.valueOf(ex.getStatusCode().value()));
        }
        if (matriculationNumberResponse.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)
                || matriculationNumberResponse.getBody() == null) {
            return new ResponseEntity<>("Student not a member of the class", HttpStatus.NOT_FOUND);
        } else if (matriculationNumberResponse.getStatusCode().isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return new ResponseEntity<>("Error when processing file occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Student std = matriculationNumberResponse.getBody();
        Attendance attendance = attendanceRepository.findByStudentIdAndSubjectIdAndDate(std.getMatriculationNumber(),
                subjectCode, LocalDate.now());
        Optional<Suspension> isSuspended = suspensionRepository
                .findByStudentIdAndSubjectId(std.getMatriculationNumber(), subjectCode);
        if (isSuspended.isPresent()) {
            return new ResponseEntity<>("Student suspended", HttpStatus.FORBIDDEN);
        }
        if (attendance == null) {
            return new ResponseEntity<>("Cannot mark attendance anymore", HttpStatus.FORBIDDEN);
        }
        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            return new ResponseEntity<>("Already marked student", HttpStatus.CONFLICT);
        }
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(attendance);
        return new ResponseEntity<>("Successfully marked attendance: student Id = " + std.getMatriculationNumber(),
                HttpStatus.OK);
    }

    public ResponseEntity<AttendanceRecordResponse> getRecord(String subjectCode, LocalDate date, int sort,
            String bearer) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Subject subject = subjectOptional.get();
        String jwtToken = jwtService.extractTokenFromHeader(bearer);
        String id = jwtService.getId(jwtToken);
        if (subjectOptional.get().getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Attendance> studentAttendance = attendanceRepository.findBySubjectIdAndDate(subjectCode, date);
        if (studentAttendance.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        switch (sort) {
            case 1 -> studentAttendance = filterAttendanceByStatus(studentAttendance, AttendanceStatus.PRESENT);
            case 2 -> studentAttendance = filterAttendanceByStatus(studentAttendance, AttendanceStatus.ABSENT);
        }
        AttendanceRecordResponse attendanceRecordResponse = buildAttendanceRecordResponse(subject, date,
                studentAttendance);
        return ResponseEntity.ok(attendanceRecordResponse);
    }

    public ResponseEntity<ByteArrayResource> getAttendanceExcel(String subjectCode, LocalDate date, int sort,
            String bearer) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Subject subject = subjectOptional.get();
        String jwtToken = jwtService.extractTokenFromHeader(bearer);
        String id = jwtService.getId(jwtToken);
        if (subjectOptional.get().getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Attendance> studentAttendance = attendanceRepository.findBySubjectIdAndDate(subjectCode, date);
        if (studentAttendance.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        switch (sort) {
            case 1 -> studentAttendance = filterAttendanceByStatus(studentAttendance, AttendanceStatus.PRESENT);
            case 2 -> studentAttendance = filterAttendanceByStatus(studentAttendance, AttendanceStatus.ABSENT);
        }
        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Records");
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = { "Matriculation Number", "Name", "Status" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Add attendance records to the sheet
        int rowNum = 1;
        for (Attendance attendance : studentAttendance) {
            Student student = studentService.getStudentById(attendance.getStudentId()).orElse(null);
            if (student == null) {
                return ResponseEntity.badRequest().build();
            }
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendance.getStudentId());
            row.createCell(1).setCellValue(student.getLastname() + " " + student.getFirstname());
            row.createCell(2).setCellValue(attendance.getStatus().toString());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write workbook to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            log.error("Error occurred ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("Error occurred ", e);
            }
        }
        // Return the Excel file as ResponseEntity
        byte[] bytes = outputStream.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(bytes);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=attendance record for " + subjectCode + ".xlsx");
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .contentLength(bytes.length)
                .contentType(org.springframework.http.MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private List<Attendance> filterAttendanceByStatus(List<Attendance> attendanceList, AttendanceStatus status) {
        return attendanceList.stream()
                .filter(attendance -> attendance.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    private AttendanceRecordResponse buildAttendanceRecordResponse(Subject subject, LocalDate date,
            List<Attendance> attendanceList) {
        AttendanceRecordResponse attendanceRecordResponse = new AttendanceRecordResponse();
        attendanceRecordResponse.setTitle(subject.getSubjectTitle());
        attendanceRecordResponse.setSubjectCode(subject.getSubjectCode());
        attendanceRecordResponse.setDate(date.toString());
        for (Attendance attendance : attendanceList) {
            attendanceRecordResponse.put(attendance.getStudentId(), attendance.getStatus());
        }
        return attendanceRecordResponse;
    }

    public ResponseEntity<StudentAttendanceRecordResponse> viewAttendanceRecord(String bearer,String code) {
        ResponseEntity<List<Attendance>> response = getStudentRecord(
                jwtService.getId(jwtService.extractTokenFromHeader(bearer)));
        if (response.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.notFound().build();
        }
        List<Attendance> attendanceList = response.getBody();
        if (attendanceList == null) {
            return ResponseEntity.internalServerError().build();
        }
        List<StudentAttendanceRecordResponse.DefaultResponse> getDefault = attendanceList.stream()
                .filter(Objects::nonNull)
                .filter(v-> v.getSubjectId().equalsIgnoreCase(code))
                .map(attendance -> {
                    Subject subject = subjectService.findSubjectByCode(attendance.getSubjectId()).orElse(null);
                    return subject != null
                            ? new StudentAttendanceRecordResponse
                                .DefaultResponse(   subject.getSubjectCode(),
                                                    subject.getSubjectTitle(),
                                                    attendance.getDate(),
                                                    attendance.getStatus())
                            : null;
                }).toList();
        return ResponseEntity.ok(new StudentAttendanceRecordResponse(
                attendanceList.get(0).getStudentId(), getDefault));
    }

    public ResponseEntity<ByteArrayResource> printAttendanceRecord(String bearer, String code) {
        ResponseEntity<StudentAttendanceRecordResponse> response = viewAttendanceRecord(bearer,code);
        if (response.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.notFound().build();
        }
        if (response.getBody() == null) {
            return ResponseEntity.noContent().build();
        }
        return buildExcel(response.getBody());
    }

    private ResponseEntity<ByteArrayResource> buildExcel(StudentAttendanceRecordResponse body) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Record");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Student ID");
        headerRow.createCell(1).setCellValue("Subject ID");
        headerRow.createCell(2).setCellValue("Date");
        headerRow.createCell(3).setCellValue("Status");
        List<StudentAttendanceRecordResponse.DefaultResponse> attendanceRecord = body.getAttendanceRecord();
        int rowNum = 1;
        for (StudentAttendanceRecordResponse.DefaultResponse attendance : attendanceRecord) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(body.getStudentId());
            row.createCell(1).setCellValue(attendance.getSubjectId());
            row.createCell(2).setCellValue(attendance.getDate().format(DateTimeFormatter.ISO_DATE));
            row.createCell(3).setCellValue(attendance.getStatus().toString());
        }
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            log.error("Error occurred ", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("filename", "attendance_record.xlsx");
        return new ResponseEntity<>(new ByteArrayResource(outputStream.toByteArray()), headers, HttpStatus.OK);
    }

    public ResponseEntity<List<Attendance>> getStudentRecord(String studentId) {
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        if (attendances == null || attendances.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(attendances);
    }
    public ResponseEntity<AvailableRecords> getRecord(String subjectCode, String bearer) {
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Subject subject = subjectOptional.get();
        String jwtToken = jwtService.extractTokenFromHeader(bearer);
        String id = jwtService.getId(jwtToken);
        if (subjectOptional.get().getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<AttendanceSetupPolicy> attendanceSetupPolicyList = attendanceSetupRepository.findAllBySubjectId(subjectCode);
        Set<AvailableRecords.Data> set = attendanceSetupPolicyList.stream()
                .map(ob -> new AvailableRecords.Data(ob.getAttendanceDate().toString())).collect(Collectors.toSet());
        return ResponseEntity.ok(new AvailableRecords(set));
    }

}
