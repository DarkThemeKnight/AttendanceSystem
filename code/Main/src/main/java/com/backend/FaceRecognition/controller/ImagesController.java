package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.face_recognition_service.FaceRecognitionService;
import com.backend.FaceRecognition.utils.EncodeImageListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/encodings")
public class ImagesController {
    private final FaceRecognitionService faceRecognitionService;

    public ImagesController(FaceRecognitionService faceRecognitionService) {
        this.faceRecognitionService = faceRecognitionService;
    }

    /**
     * Retrieves encoded images for students enrolled in a specific subject.
     * This endpoint retrieves the encoded images of students enrolled in the subject identified by the provided code.
     * It delegates the retrieval of encoded images to the faceRecognitionService by invoking the getEncodings method
     * with the provided subject code. If the subject is found, it returns a response containing the encoded images
     * of the students enrolled in the subject.
     *
     * @param code The code of the subject for which encoded images are requested.
     * @return A ResponseEntity containing the encoded images of students enrolled in the subject:
     *         - If the subject is found and encoded images are retrieved successfully, returns OK (200) status
     *           along with the encoded images in the response body.
     *         - If the subject is not found, returns a NOT_FOUND (404) status.
     */
    @GetMapping
    public ResponseEntity<EncodeImageListResponse> getEncodingsBySubject(@RequestParam("code") String code) {
        return faceRecognitionService.getEncodings(code);
    }


}
