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
import com.backend.FaceRecognition.utils.*;
import com.backend.FaceRecognition.utils.history.AttendanceRecordHistoryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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
    @Lazy
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
    public ResponseEntity<InitializeAttendanceResponse> initializeAttendance(String subjectCode, String authorization, int duration) {
        Optional<AttendanceSetupPolicy> setupPolicy = attendanceSetupRepository.findBySubjectIdAndAttendanceDate(subjectCode,LocalDate.now());
        if (setupPolicy.isPresent()) {
            return ResponseEntity.badRequest().body(
                    InitializeAttendanceResponse.builder()
                            .status("FAILED")
                            .message("attendance already initialized TODAY!")
                            .metaData(InitializeAttendanceResponse.Metadata.builder()
                                    .subjectId(setupPolicy.get().getSubjectId())
                                    .attendanceCode(setupPolicy.get().getCode())
                                    .totalDurationInMinutes(setupPolicy.get().getDuration()+"")
                                    .build())
                            .build());
        }
        log.info("Duration => {}",duration);
        if (duration < 10) {
            return ResponseEntity.badRequest().body(
                    InitializeAttendanceResponse.builder()
                            .status("FAILED")
                            .message("Duration at least 10 minutes")
                            .build());
        }
        String jwtToken = jwtService.extractTokenFromHeader(authorization);
        String id = jwtService.getId(jwtToken);
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>(InitializeAttendanceResponse.builder()
                    .status("FAILED")
                    .message("Subject not found")
                    .build(),HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            return new ResponseEntity<>(InitializeAttendanceResponse.builder()
                    .status("FAILED")
                    .message("Unauthorized to take attendance")
                    .build(),HttpStatus.UNAUTHORIZED);
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
        return new ResponseEntity<>(InitializeAttendanceResponse.builder()
                .status("SUCCESS")
                .message("Initialized attendance successfully")
                .metaData(InitializeAttendanceResponse.Metadata.builder()
                        .subjectId(setup.getSubjectId())
                        .attendanceCode(setup.getCode())
                        .creationDateTime(LocalDateTime.now().toString())
                        .expiryDateTime(LocalDateTime.now().plusMinutes(setup.getDuration()).toString())
                        .totalDurationInMinutes(String.valueOf(setup.getDuration()))
                        .build())
                .build(),HttpStatus.UNAUTHORIZED);
    }
    public ResponseEntity<String> initializeAttendance(String subjectCode, String authorization, int duration, LocalDate date) {
        List<Attendance> attendances = attendanceRepository.findBySubjectIdAndDate(subjectCode, date);
        if (!attendances.isEmpty()) {
            return ResponseEntity.badRequest().body("attendance already initialized");
        }
        if (duration < 10) {
            return ResponseEntity.badRequest().body("Duration at least 10 minutes");
        }
//        String jwtToken = jwtService.extractTokenFromHeader(authorization);
        String id = jwtService.getId(authorization);
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            return new ResponseEntity<>("Unauthorized to take attendance", HttpStatus.UNAUTHORIZED);
        }
        Set<Student> allPossibleAttendees = new HashSet<>(studentService.getAllStudentsOfferingCourse2(subjectCode));
        List<Attendance> studentAttendance = allPossibleAttendees.stream()
                .map(student -> new Attendance(student.getMatriculationNumber(),
                        subject.getSubjectCode(),
                        date,
                        AttendanceStatus.ABSENT))
                .toList();
        AttendanceSetupPolicy setup = AttendanceSetupPolicy.builder()
                .code(UniqueCodeGenerator.generateCode(10))
                .duration(duration)
                .subjectId(subjectCode)
                .attendanceDate(date)
                .attendanceDateTime(LocalDateTime.of(date, LocalTime.now()))
                .build();
        setup = attendanceSetupRepository.save(setup);
        attendanceRepository.saveAll(studentAttendance);
        return new ResponseEntity<>("code="+setup.getCode(), HttpStatus.OK);
    }
    @SneakyThrows
    public ResponseEntity<String> updateAttendanceStatus(String attendanceCode, String studentId, AttendanceStatus status) {
        Optional<AttendanceSetupPolicy> attendanceSetup =
                attendanceSetupRepository.findById(attendanceCode);
        if (attendanceSetup.isEmpty()) {
            return ResponseEntity.badRequest().body("Attendance is not initialized yet");
        }
        var policy =attendanceSetup.get();
        Attendance attendance = attendanceRepository.findByStudentIdAndSubjectIdAndDate(studentId,policy.getSubjectId(),policy.getAttendanceDate());
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
        return ResponseEntity.ok(new ObjectMapper().writeValueAsString(new Response("Success")));
    }
        public ResponseEntity<String> updateAttendanceStatus(String attendanceCode,
                                                         MultipartFile multipartFile) {
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
                    subjectCode);
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
        }catch (HttpClientErrorException | HttpServerErrorException ex) {
        if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND) || ex.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
            return new ResponseEntity<>("Student not a member of the class", HttpStatus.NOT_FOUND);
        } else if (ex.getStatusCode().isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return new ResponseEntity<>("Error when processing file occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        }catch (Exception e) {
        return new ResponseEntity<>("Failed to mark attendance", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.badRequest().build();
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

        var record = getRecord(subjectCode,date,sort,bearer);
        if (!record.getStatusCode().equals(HttpStatus.OK)){
            return ResponseEntity.badRequest().build();
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Records");
        Row headerRow = sheet.createRow(0);
        String[] headers = { "Matriculation Number", "Name", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        int rowNum = 1;
        var body = Objects.requireNonNull(record.getBody()).getAttendanceData();
        for (var attendance : body) {
            Student student = studentService.getStudentById(attendance.getMatriculationNumber()).orElse(null);
            if (student == null) {
                return ResponseEntity.badRequest().build();
            }
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendance.getMatriculationNumber());
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
        attendanceRecordResponse.setAttendanceData(
                attendanceList.stream().map(v
                        -> {
                    Student student = studentService.getStudentById(v.getStudentId()).orElse(null);
                    if (student == null){
                        return null;
                    }
                    return AttendanceRecordResponse.MetaData.builder()
                            .firstname(student.getFirstname())
                            .lastname(student.getLastname())
                            .matriculationNumber(v.getStudentId())
                            .status(v.getStatus())
                            .build();
                }
                ).collect(Collectors.toList())
        );
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
    public ResponseEntity<AttendanceRecordHistoryResponse> getHistoryRecord(String subjectCode, String bearer){
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
        var data= attendanceRepository.findByStudentId(subject.getSubjectCode());
        final Map<String,Double> userScore = new HashMap<>();
        int total = 0;
        try {
            total = Objects.requireNonNull(getRecord(subjectCode, bearer).getBody()).getData().size();
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
        data.forEach(
                attendance -> {
                    userScore.computeIfPresent(
                            attendance.getStudentId(),(k,v) -> attendance.getStatus().equals(AttendanceStatus.PRESENT)?v+1.0:v+0.0
                    );
                    userScore.putIfAbsent(attendance.getStudentId(), attendance.getStatus().equals(AttendanceStatus.PRESENT)?1.0:0.0);
                }
        );
        final int totalFinal = total;
        var keys =userScore.keySet();
        keys.forEach(key -> {
            userScore.compute(key, (k,v)-> v!=null? (100.0 * v) /totalFinal : 0);
        });
        List<AttendanceRecordHistoryResponse.MetaData> metaDataList = new ArrayList<>();
        for (Map.Entry<String,Double> entry: userScore.entrySet()) {
            Student student = studentService.getStudentById(entry.getKey()).get();
            AttendanceRecordHistoryResponse.MetaData  metaData = AttendanceRecordHistoryResponse.MetaData.builder()
                    .firstname(student.getFirstname())
                    .lastname(student.getLastname())
                    .matriculationNumber(student.getMatriculationNumber())
                    .percentageAttendanceScore(String.format("%.2f",entry.getValue())+"%")
                    .isEligibleForExam(entry.getValue()-70.0 > 0.0001 ?"YES":"NO")
                    .build();
            metaDataList.add(metaData);
        }
        AttendanceRecordHistoryResponse generateHistory = AttendanceRecordHistoryResponse.builder()
                .title(subject.getSubjectTitle())
                .subjectCode(subject.getSubjectCode())
                .attendanceData(
                        metaDataList
                ).build();
        return ResponseEntity.ok(generateHistory);
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
        List<AttendanceSetupPolicy> attendanceSetupPolicyList = attendanceSetupRepository
                .findAllBySubjectId(subjectCode);

        Set<AvailableRecords.Data> set = attendanceSetupPolicyList.stream()
                .map(ob -> new AvailableRecords.Data(ob.getAttendanceDate().toString()))
                .collect(Collectors.toSet());

        List<AvailableRecords.Data> sortedList = set.stream()
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate())))
                .toList();

        set = new HashSet<>(sortedList);

        return ResponseEntity.ok(new AvailableRecords(set));
    }
}
