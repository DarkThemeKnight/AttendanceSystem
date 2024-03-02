package com.backend.FaceRecognition.services.face_recognition_service;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.image_request_service.EncodingService;
import com.backend.FaceRecognition.services.student.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.*;

import com.backend.FaceRecognition.utils.student.StudentRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class FaceRecognitionService {
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final EncodingService encodingService;
    private final FaceRecognitionEndpoints faceRecognitionEndpoints;
    public FaceRecognitionService(StudentService studentService, SubjectService subjectService,
                                  EncodingService encodingService, FaceRecognitionEndpoints faceRecognitionEndpoints) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.encodingService = encodingService;
        this.faceRecognitionEndpoints = faceRecognitionEndpoints;
    }
    /**
     * Retrieves the encodings for students enrolled in a subject.
     * This method retrieves the encodings for students enrolled in the specified subject identified by its code.
     * It first attempts to find the subject by its code.
     * If the subject is found, it retrieves the matriculation
     * numbers of the students enrolled in the subject
     * and returns them as a response containing encoded image data.
     * If the subject is not found, a not found status is returned.
     *
     * @param subjectCode The code of the subject for which encodings are requested.
     * @return A ResponseEntity containing the response object EncodeImageListResponse with the encoded image data
     *         for students enrolled in the subject if the subject is found, with a status of OK (200).
     *         If the subject is not found, a not found status is returned.
     */
    public ResponseEntity<EncodeImageListResponse> getEncodings(String subjectCode) {
        Subject subject = subjectService.findSubjectByCode(subjectCode).orElse(null);
        if (subject == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<String> matriculationNumbers = subject.getStudents().stream()
                .map(Student::getMatriculationNumber)
                .toList();
        return new ResponseEntity<>(getResponse(matriculationNumbers), HttpStatus.OK);
    }

    /**
     * Creates an EncodeImageListResponse object containing encoded image data for student matriculation numbers.
     * This method creates an EncodeImageListResponse object containing the encoded image data for the specified
     * list of student matriculation numbers.
     * It retrieves the encoded image data using the encoding service.
     *
     * @param matriculationNumbers The list of matriculation numbers for which encoded image data is requested.
     * @return An EncodeImageListResponse object containing encoded image data for student matriculation numbers.
     */
    private EncodeImageListResponse getResponse(List<String> matriculationNumbers) {
        List<EncodedImageResponse> imageResponses = matriculationNumbers.stream()
                .map(encodingService::getStudentEncodings)
                .filter(listResponseEntity -> listResponseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK))
                .map(ResponseEntity::getBody)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
        EncodeImageListResponse request = new EncodeImageListResponse();
        imageResponses.forEach(eI -> request.add(eI.getMatriculationNumber(), eI.getData()));
        return request;
    }

    /**
     * Recognizes a face from the provided image file.
     * This method sends a face recognition request to the configured endpoint along with the image file
     * and subject ID. It authenticates the request using the bearer token provided in the authorization header.
     * If the recognition is successful and returns a student request, it retrieves the corresponding student
     * information from the database based on the matriculation number provided in the response.
     *
     * @param file      The image file containing the face to be recognized.
     * @param subjectId The ID of the subject associated with the recognition.
     * @param bearer    The bearer token used for authentication.
     * @return A ResponseEntity containing the recognized Student object if successful, or appropriate status
     *         codes indicating errors such as not found, internal server error, or unauthorized.
     * @throws IOException If an I/O exception occurs while processing the image file.
     */
    public ResponseEntity<Student> recognizeFace(MultipartFile file, String subjectId, String bearer)
            throws IOException {
        String endpoint = faceRecognitionEndpoints.getEndpoint("rec") + "?subject_id=" + subjectId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", bearer);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Assuming getResource() gives InputStreamResource
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<StudentRequest> responseEntity = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                requestEntity,
                StudentRequest.class);

        if (responseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
            StudentRequest val = responseEntity.getBody();
            if (val == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Student st = studentService.getStudentById(val.getMatriculationNumber()).orElse(null);
            if (st == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(st, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }


}