package com.backend.FaceRecognition.services.authorization_service.admin;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.EncodedImages;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.EncodedImagesRepository;
import com.backend.FaceRecognition.services.data_persistence_service.ApplicationUserService;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.AddStudentRequest;
import com.backend.FaceRecognition.utils.EncodedImage;
import com.backend.FaceRecognition.utils.FaceRecognitionEndpoints;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {
    private final EncodedImagesRepository encodedImagesRepository;
    private final ApplicationUserService applicationUserService;
    private final SubjectService subjectService;
    private final StudentService studentService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final FaceRecognitionEndpoints faceRecognitionEndpoints;

    public AdminService(ApplicationUserService applicationUserService, SubjectService subjectService,
                        StudentService studentService, EncodedImagesRepository encodedImagesRepository, FaceRecognitionEndpoints faceRecognitionEndpoints) {
        this.encodedImagesRepository = encodedImagesRepository;
        this.applicationUserService = applicationUserService;
        this.subjectService = subjectService;
        this.studentService = studentService;
        this.faceRecognitionEndpoints = faceRecognitionEndpoints;
    }
    /**
     * This method locks the user account identified by the provided ID.
     * If the user is found, the method checks if the user has roles of ROLE_SUPER_ADMIN
     * or ROLE_ADMIN.
     * If the user does not have these roles, the account is locked
     * by setting the 'enabled' attribute to false.
     * If the account is successfully locked,
     * a success response (200) is returned.
     * If the user is not found, a not found response (404) is returned.
     * If the user has unauthorized roles, an unauthorized response (401) is returned.
     *
     * @param id The ID of the user account to be locked.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the account is locked successfully, a success response (200) is returned.
     *         If the user is not found, a not found response (404) is returned.
     *         If the user has unauthorized roles, an unauthorized response (401) is returned.
     */
    public ResponseEntity<String> lockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN) || user.hasRole(Role.ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to lock account.");
            }
            user.setEnabled(false);
            applicationUserService.update(user);
            return ResponseEntity.ok("Account locked successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Unlocks the user account identified by the provided ID.
     * This method unlocks the user account identified by the provided ID.
     * If the user is found, the method checks if the user has roles of ROLE_SUPER_ADMIN
     * or ROLE_ADMIN.
     * If the user does not have these roles, the account is unlocked
     * by setting the 'enabled' attribute to true.
     * If the account is successfully unlocked,
     * a success response (200) is returned.
     * If the user is not found, a not found response (404)
     * is returned.
     * If the user has unauthorized roles, an unauthorized response (401) is returned.
     *
     * @param id The ID of the user account to be unlocked.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the account is unlocked successfully, a success response (200) is returned.
     *         If the user is not found, a not found response (404) is returned.
     *         If the user has unauthorized roles, an unauthorized response (401) is returned.
     */
    public ResponseEntity<String> unlockAccount(String id) {
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            if (user.hasRole(Role.ROLE_SUPER_ADMIN) || user.hasRole(Role.ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to lock account.");
            }
            user.setEnabled(true);
            applicationUserService.update(user);
            return ResponseEntity.ok("Account unlocked successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Adds a new subject based on the provided subject request.
     * This method attempts to add a new subject based on the provided subject request.
     * It first checks if a subject with the same code already exists.
     * If so, it returns
     * a conflict response (409).
     * Otherwise, it parses the subject request and attempts
     * to save the subject.
     * If the lecturer associated with the subject is not found, it
     * returns a not found response (404).
     * If the subject is saved successfully, it returns
     * an OK response (200).
     *
     * @param request The subject request containing the details of the subject to be added.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the subject is already added, a conflict response (409) is returned.
     *         If the lecturer associated with the subject is not found, a not found response (404)
     *         is returned.
     *         If the subject is saved successfully, an OK response (200) is returned.
     */
    public ResponseEntity<String> addSubject(SubjectRequest request) {
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isPresent()) {
            return new ResponseEntity<>("Subject already added", HttpStatus.CONFLICT);
        }
        Subject subject = parse(request);
        if (subject == null) {
            return new ResponseEntity<>("Lecturer not found", HttpStatus.NOT_FOUND);
        }
        subjectService.saveSubject(subject);
        return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
    }
    /**
     * Updates an existing subject based on the provided subject request.
     * This method attempts to update an existing subject based on the provided subject request.
     * It first checks if the subject with the provided subject code exists. If not, it returns
     * a conflict response (409). Otherwise, it retrieves the subject, updates its details based
     * on the provided request, and attempts to save the changes. If the lecturer ID provided in
     * the request is not found, it returns a not found response (404). If the subject is updated
     * successfully, it returns an OK response (200).
     *
     * @param request The subject request containing the updated details of the subject.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the subject does not exist, a conflict response (409) is returned.
     *         If the lecturer ID provided in the request is not found, a not found response (404)
     *         is returned.
     *         If the subject is updated successfully, an OK response (200) is returned.
     */
    public ResponseEntity<String> updateSubject(SubjectRequest request) {
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isEmpty()) {
            return new ResponseEntity<>("Subject does not exist", HttpStatus.CONFLICT);
        }
        ApplicationUser user = null;
        if (request.getIdLecturerInCharge() != null && !request.getIdLecturerInCharge().isEmpty()) {
            user = applicationUserService.findUser(request.getIdLecturerInCharge()).orElse(null);
            if (user == null) {
                return new ResponseEntity<>("Lecturer not found", HttpStatus.NOT_FOUND);
            }
        }
        Subject subject = subject1.get();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectTitle(request.getSubjectTitle());
        subject.setLecturerInCharge(user);
        subjectService.saveSubject(subject);
        return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
    }
    /**
     * Deletes an existing subject based on the provided subject code.
     * This method attempts to delete an existing subject based on the provided subject code.
     * It first checks if the subject with the provided subject code exists. If not, it returns
     * a conflict response (409). Otherwise, it deletes the subject and returns a success response (200).
     *
     * @param request The subject request containing the subject code of the subject to be deleted.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the subject does not exist, a conflict response (409) is returned.
     *         If the subject is deleted successfully, an OK response (200) is returned.
     */
    public ResponseEntity<String> deleteSubject(SubjectRequest request) {
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isEmpty()) {
            return new ResponseEntity<>("Subject does not exist", HttpStatus.CONFLICT);
        }
        subjectService.deleteSubjectByCode(request.getSubjectCode());
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }
    /**
     * Retrieves details of a subject based on the provided subject code.
     * This method retrieves details of a subject based on the provided subject code.
     * It attempts to find the subject in the database using the provided subject code.
     * If the subject is found, it parses the subject details into a response object
     * and returns an OK response (200). If the subject is not found, a not found response
     * (404) is returned.
     *
     * @param subjectCode The code of the subject for which details are to be retrieved.
     * @return A ResponseEntity containing details of the subject as a SubjectResponse object.
     *         If the subject is found, the details are returned along with an OK response (200).
     *         If the subject is not found, a not found response (404) is returned.
     */
    public ResponseEntity<SubjectResponse> getSubject(String subjectCode) {
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        log.info("getting subject");
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>(new SubjectResponse("message"),
                    HttpStatus.NOT_FOUND);
        }
        SubjectResponse response = parse(optionalSubject.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public Subject parse(SubjectRequest request) {
        ApplicationUser user = null;
        if (request.getIdLecturerInCharge() != null) {
            user = applicationUserService.findUser(request.getIdLecturerInCharge()).orElse(null);
            if (user == null) {
                return null;
            }
        }
        Subject subject = new Subject();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectTitle(request.getSubjectTitle());
        subject.setLecturerInCharge(user);
        return subject;
    }

    public SubjectResponse parse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setSubjectCode(subject.getSubjectCode());
        response.setSubjectTitle(subject.getSubjectTitle());
        Set<Student> students = subject.getStudents();
        // log.info("size {}",students.size());
        Set<String> matriculationNum = students.stream()
                .map(Student::getMatriculationNumber).collect(Collectors.toSet());
        response.setStudents(matriculationNum);
        if (subject.getLecturerInCharge() != null) {
            response.setIdLecturerInCharge(subject.getLecturerInCharge().getId());
        }
        return response;
    }

    public ResponseEntity<String> clearSubjectStudents(String subjectCode) {
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>("subject Not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = optionalSubject.get();
        subject.clearStudents();
        subjectService.saveSubject(subject);
        return new ResponseEntity<>("Cleared", HttpStatus.OK);
    }
    /**
     * Clears the list of students enrolled in a subject based on the provided subject code.
     * This method clears the list of students enrolled in a subject based on the provided subject code.
     * It attempts to find the subject in the database using the provided subject code.
     * If the subject is found, its list of students is cleared, and the changes are saved to the database.
     * A success response (200) is returned after the operation.
     * If the subject is not found, a not found
     * response (404) is returned.
     *
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the subject is found and the list of students is cleared successfully, a success
     *         response (200) is returned.
     *         If the subject is not found, a not found response (404) is returned.
     */
    public ResponseEntity<String> clearAllStudentSubjects() {
        List<Subject> subjects = subjectService.findAllSubjects();
        subjects.forEach(Subject::clearStudents);
        subjectService.saveSubject(subjects);
        return new ResponseEntity<>("Cleared successfully", HttpStatus.OK);
    }
    /**
     * Adds a new student based on the provided student request.
     *
     * This method attempts to add a new student based on the provided student request.
     * It first checks if a student with the same matriculation number already exists.
     * If not, it parses the student details from the request and saves the student.
     * If the student is saved successfully, it returns an OK response (200).
     * If the student with the same matriculation number already exists, it returns
     * a conflict response (409).
     *
     * @param request The request containing the details of the student to be added.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the student is saved successfully, an OK response (200) is returned.
     *         If a student with the same matriculation number already exists, a conflict
     *         response (409) is returned.
     */
    public ResponseEntity<String> addStudent(AddStudentRequest request) {
        Optional<Student> studentOptional = 
            studentService.getStudentById(request.getMatriculationNumber());
        if (studentOptional.isEmpty()) {
            Student student = parse(request);
            studentService.saveStudent(student);
            return new ResponseEntity<>("Saved", HttpStatus.OK);
        }
        return new ResponseEntity<>("Student already present", HttpStatus.CONFLICT);
    }
    /**
     * Adds an image of a student identified by their student ID.
     * This method sends a request to a FastAPI endpoint for face recognition to process
     * the provided image file.
     * It then saves the processed image along with the student ID
     * in the database.
     * If the student is not found, it returns a not found response (404).
     * If the image processing is successful, it saves the processed image and returns
     * a success response (200).
     * If there are errors during image processing or if the
     * processed image is invalid, it returns a bad request response (400).
     *
     * @param file      The image file to be processed.
     * @param studentId The ID of the student to whom the image belongs.
     * @return A ResponseEntity containing a message indicating the result of the operation.
     *         If the student is not found, a not found response (404) is returned.
     *         If the image processing is successful and the image is saved, a success response (200) is returned.
     *         If there are errors during image processing or if the processed image is invalid, a bad request response (400) is returned.
     */
    @Transactional
    public ResponseEntity<String> addStudentImage(MultipartFile file, String studentId) {
        // send request to face recognition
        Student student = studentService.getStudentById(studentId).orElse(null);
        if (student == null) {
            return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
        }
        String url = faceRecognitionEndpoints.getEndpoint("ip")+"?student_id="+studentId; // Your FastAPI endpoint URL
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Assuming getResource() gives InputStreamResource
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<EncodedImage> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                EncodedImage.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            EncodedImage image = responseEntity.getBody();
            if (image == null) {
                return new ResponseEntity<>("Bad Image Could not encode image", HttpStatus.BAD_REQUEST);
            }
            if (image.getMessage().equals("Invalid Amount of Faces detected")) {
                return  new ResponseEntity<>("Invalid amount of faces detected",HttpStatus.BAD_REQUEST);
            }
            EncodedImages imageEntity = EncodedImages.builder()
                    .data(image.getData())
                    .matriculationNumber(studentId)
                    .build();
            encodedImagesRepository.save(imageEntity);
            return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error processing file", HttpStatus.BAD_REQUEST);
        }
    }

    public Student parse(AddStudentRequest request) {
        Student student = new Student();
        student.setFirstname(request.getFirstname());
        student.setLastname(request.getLastname());
        student.setMatriculationNumber(request.getMatriculationNumber());
        student.setMiddleName(request.getMiddleName());
        student.setSchoolEmail(request.getSchoolEmail());
        return student;
    }

}
