package com.backend.FaceRecognition.helper;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.*;
import com.backend.FaceRecognition.repository.AttendanceSetupPolicyRepository;
import com.backend.FaceRecognition.repository.NotificationRepository;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.services.authorization_service.lecturer_service.LecturerService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.mail.MailService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.FaceRecognitionEndpoints;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.google.common.io.Files;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class InitializeBeans {
    private final ApplicationUserService applicationUserService;
    private final AttendanceSetupPolicyRepository attendanceSetupRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceService attendanceService;
    private final JwtService jwtService;
    private final LecturerService lecturerService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;
    private final EntityManager entityManager;

    private final AuthenticationService authenticationService;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String type;
    private void setNotifications(){
        Notification notification = new Notification();
        notification.setValidUntil(LocalDate.now().plusDays(3));
        notification.setTitle("Welcome");
        notification.setContent("You are welcome to the attendance management system");
        notificationRepository.save(notification);
    }
    private void setupSuperAdmin() {
        log.info("Setting up Super Admin...");
        ApplicationUser user = new ApplicationUser(
                "0001",
                "Omotola",
                "David",
                "Ayanfeoluwa",
                "ayanfeoluwadafidi@outlook.com",
                passwordEncoder.encode("141066"),
                "Demo Address",
                "08055132800",
                Set.of(Role.ROLE_SUPER_ADMIN),
                true,
                true,
                true,
                true,
                null);
        applicationUserService.create(user);
        log.info("Super Admin setup complete.");
    }

    private void setupAdmin() {
        log.info("Setting up Admin...");
        var user = ApplicationUser.builder()
                .id("0002")
                .address("Demo Address")
                .phoneNumber("029203939202")
                .userRole(Set.of(Role.ROLE_ADMIN))
                .firstname("admin1")
                .lastname("demoSurname")
                .password(passwordEncoder.encode("DEMOSURNAME"))
                .isAccountNonExpired(true)
                .isEnabled(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .build();
        applicationUserService.create(user);
        log.info("Admin setup complete.");
    }

    private void setupLecturer() {
        log.info("Setting up Lecturer...");
        var user = ApplicationUser.builder()
                .id("i0001")
                .address("Demo Address")
                .phoneNumber("029203939202")
                .userRole(Set.of(Role.ROLE_LECTURER))
                .firstname("Ayanfeoluwa")
                .lastname("David")
                .schoolEmail("ayanfeoluwadafidi@gmail.com")
                .password(passwordEncoder.encode("DEFAULT"))
                .isAccountNonExpired(true)
                .isEnabled(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .build();
        applicationUserService.create(user);
        log.info("Lecturer setup complete.");
    }

    @SneakyThrows
    private void mockDB() {
        log.info("Mocking database with subjects and students...");
        Subject subject = Subject.builder()
                .subjectCode("MAT101")
                .lecturerInCharge(applicationUserService.findUser("i0001").orElseThrow())
                .subjectTitle("Calculus I")
                .build();
        subjectService.save(subject);

        Subject subject2 = Subject.builder()
                .subjectCode("MAT102")
                .lecturerInCharge(applicationUserService.findUser("i0001").orElseThrow())
                .subjectTitle("Calculus I")
                .build();
        subjectService.save(subject2);

        String lecturerToken = authenticationService.login(new AuthenticationRequest(subject.getLecturerInCharge().getId(), "DEMOSURNAME")).getBody().getJwtToken();
        List<Student> savedStudents = new ArrayList<>();
        File[] imagesDir = new File(getClass().getResource("/images").getPath()).listFiles();
        AtomicInteger integer = new AtomicInteger(0);
        assert imagesDir != null;
        int halver = 1;
        for (File imageDir : imagesDir) {
            String[] names = imageDir.getName().split("_");
            ApplicationUser applicationUser = ApplicationUser.builder()
                    .id(integer.incrementAndGet() + "")
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isAccountNonExpired(true)
                    .userRole(Collections.singleton(Role.ROLE_STUDENT))
                    .address("Demo Address")
                    .phoneNumber("demo phone number")
                    .firstname(names[0])
                    .lastname(names[1])
                    .password(passwordEncoder.encode(names[1].toUpperCase()))
                    .schoolEmail(names[0] + names[1] + integer.get() + "@gmail.com")
                    .build();
            Student student = Student.builder()
                    .matriculationNumber(applicationUser.getId())
                    .schoolEmail(applicationUser.getSchoolEmail())
                    .lastname(applicationUser.getLastname())
                    .firstname(applicationUser.getFirstname())
                    .build();
            applicationUserService.create(applicationUser);
            studentService.saveStudent(student);
            if (halver++ /2 % 2 == 0){
                lecturerService.addStudentToSubject2(lecturerToken, student.getMatriculationNumber(), subject2.getSubjectCode());
            }
            lecturerService.addStudentToSubject2(lecturerToken, student.getMatriculationNumber(), subject.getSubjectCode());
            savedStudents.add(student);
            var authenticationResponseEntity = authenticationService.login(new AuthenticationRequest(applicationUser.getId(), applicationUser.getLastname().toUpperCase()));
            if (authenticationResponseEntity.getStatusCode().is2xxSuccessful()) {
                String studentToken = Objects.requireNonNull(authenticationResponseEntity.getBody()).getJwtToken();
                File[] images = imageDir.listFiles();
                assert images != null;
                Arrays.stream(images).forEach(image -> {
                    try {
                        ByteArrayResource byteArrayResource = new ByteArrayResource(Files.toByteArray(image)) {
                            @Override
                            public String getFilename() {
                                return image.getName() + ".jpg";
                            }
                        };
                        log.info("Adding image encodings for student ID => {}, Name => {}", applicationUser.getId(), applicationUser.getFirstname() + applicationUser.getLastname());
                        var response = studentService.addStudentImage(byteArrayResource, studentToken);
                        log.info("Result => {}", response.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILED");
                    } catch (IOException ignored) {
                        log.warn("Failed to process image for student ID => {}, Name => {}", applicationUser.getId(), applicationUser.getFirstname() + applicationUser.getLastname());
                    }
                });
            } else {
                log.error("Authentication failed for student ID => {}, Name => {}", applicationUser.getId(), applicationUser.getFirstname() + applicationUser.getLastname());
                throw new RuntimeException();
            }
        }
        initializeAttendances(savedStudents, subject, lecturerToken);

    }

    public void initializeAttendances(List<Student> savedStudents, Subject subject, String lecturerToken) {
        log.info("Initializing attendances...");
        Random random = new Random();
        Set<LocalDate> usedDates = new HashSet<>();
        int attempts = 0;
        while (usedDates.size() < 30 && attempts < 365) { // Prevent infinite loop
            LocalDate randomDate = LocalDate.now().minusDays(random.nextInt(365));
            if (usedDates.add(randomDate)) {
                int attendanceSize = 20 + random.nextInt(21); // Random size between 20 and 40
                var responseEntity = attendanceService.initializeAttendance(subject.getSubjectCode(), lecturerToken, attendanceSize, randomDate);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    String code = Objects.requireNonNull(responseEntity.getBody()).split("=")[1];
                    int size = 10 + random.nextInt(savedStudents.size() - 10); // Random sublist size between 10 and savedStudents.size()
                    Collections.shuffle(savedStudents);
                    List<Student> sublist = savedStudents.subList(0, size);
                    sublist.forEach(v -> {
                        var result =attendanceService.updateAttendanceStatus(code, v.getMatriculationNumber(), AttendanceStatus.PRESENT);
                    });
                    log.info("Attendance initialized for date => {}", randomDate);
                } else {
                    log.warn("Failed to initialize attendance for date => {}", randomDate);
                }
            }
            attempts++;
        }
    }

    @Bean
    public CommandLineRunner setupApplication() {
        return args -> {
            if (type.equals("create-drop")) {
                log.info("Setting up application...");
                setupAdmin();
                setupLecturer();
                setupSuperAdmin();
                mockDB();
                setNotifications();
                log.info("Application setup complete.");
            }
        };

    }

    @Bean
    public FaceRecognitionEndpoints initializeEndpoints() {
        log.info("Initializing Face Recognition Endpoints...");
        Map<String, String> endpointMap = new HashMap<>();
        endpointMap.put("ip", "http://localhost:8000/api/v1/image-processing");
        endpointMap.put("rec", "http://localhost:8000/api/v1/recognize");
        log.info("Face Recognition Endpoints initialized.");
        return new FaceRecognitionEndpoints(endpointMap);
    }

    @Scheduled(cron = "0 */15 8-16 * * ?") //Every 15 min between 8am - 6pm
    private void transmitMail(){
        log.info("Sending initialize attendance mails ..................");
        List<AttendanceSetupPolicy> policies = attendanceSetupRepository.findAll();
        policies.stream().filter(policy-> policy.getAttendanceDateTime()
                        .isAfter(LocalDateTime.now().minusHours(1)))
                .filter(policy-> LocalDateTime.now().isAfter(policy.getAttendanceDateTime().plusMinutes(policy.getDuration())))
                .forEach(policy-> {
                    Optional<Subject> subjectOptional = subjectService.findSubjectByCode(policy.getSubjectId());
                    if (subjectOptional.isPresent()) {
                        Subject subject= subjectOptional.get();
                        Date date = JwtService.getDate(3, 'M');
                        String minBearer = jwtService.generate(new HashMap<>(), subject.getLecturerInCharge(), date);
                        ByteArrayResource excelFile = attendanceService.getAttendanceExcel(policy.getSubjectId(), policy.getAttendanceDate(), 0, "Bearer " + minBearer).getBody();
                        mailService.sendAttendanceCompletionMail(excelFile, subject.getLecturerInCharge().getSchoolEmail());
                    }
                });
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void cleanupExpiredNotifications() {
        log.info("Cleaning up Expired Notifications................");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<Notification> deleteQuery = cb.createCriteriaDelete(Notification.class);
        Root<Notification> notificationRoot = deleteQuery.from(Notification.class);
        deleteQuery.where(cb.lessThan(notificationRoot.get("validUntil"), LocalDate.now()));
        entityManager.createQuery(deleteQuery).executeUpdate();
        log.info("Done");
    }

}
