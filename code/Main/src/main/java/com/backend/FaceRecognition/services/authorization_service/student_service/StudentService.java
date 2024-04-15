package com.backend.FaceRecognition.services.authorization_service.student_service;
import com.backend.FaceRecognition.entities.EncodedImages;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.repository.EncodedImagesRepository;
import com.backend.FaceRecognition.repository.StudentRepository;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.utils.EncodedImage;
import com.backend.FaceRecognition.utils.FaceRecognitionEndpoints;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
@Service
public class StudentService {
    private final EncodedImagesRepository encodedImagesRepository;
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final FaceRecognitionEndpoints faceRecognitionEndpoints;
    private final JwtService jwtService;

    public StudentService(EncodedImagesRepository encodedImagesRepository, StudentRepository studentRepository, FaceRecognitionEndpoints faceRecognitionEndpoints,  JwtService jwtService) {
        this.encodedImagesRepository = encodedImagesRepository;
        this.studentRepository = studentRepository;
        this.faceRecognitionEndpoints = faceRecognitionEndpoints;
        this.jwtService = jwtService;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    public Optional<Student> getStudentById(String matriculationNumber) {
        return studentRepository.findById(matriculationNumber);
    }
    public void saveStudent(Student student) {
        studentRepository.save(student);
    }
    public void saveAll(Collection<Student> student) {
         studentRepository.saveAll(student);
    }
    @Transactional
    public ResponseEntity<String> addStudentImage(MultipartFile file, String auth) {
        String token = jwtService.extractTokenFromHeader(auth);
        String studentId = jwtService.getId(token);
        // send request to face recognition
        Student student =  getStudentById(studentId).orElse(null);
        if (student == null) {
            return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
        }
        String url = faceRecognitionEndpoints.getEndpoint("ip")+"?student_id="+studentId; // Your FastAPI endpoint URL
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Assuming getResource() gives InputStreamResource
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<EncodedImage> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    EncodedImage.class);
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

        }catch (HttpClientErrorException.BadRequest e){
            String responseBody = e.getResponseBodyAs(String.class);
            return ResponseEntity.badRequest().body(responseBody);
        }
    }
    public Set<Student> getAllStudentsOfferingCourse(String subjectCode) {
        return studentRepository.findAllStudentsBySubjectCode(subjectCode);
    }
}
