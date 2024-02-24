package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.image_request_service.ImageRequestService;
import com.backend.FaceRecognition.utils.image.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/images")
public class ImageController {

    private final ImageRequestService imageRequestService;
    @Autowired
    public ImageController(ImageRequestService imageRequestService) {
        this.imageRequestService = imageRequestService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ImageIO>> getAllImages() {
        List<ImageIO> images = imageRequestService.getAllImages();
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    @GetMapping("/faculty/{faculty}")
    public ResponseEntity<List<ImageIO>> getAllFromFaculty(@PathVariable String faculty) {
        List<ImageIO> images = imageRequestService.getAllFromFaculty(faculty);
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<ImageIO>> getAllFromDepartment(@PathVariable String department) {
        List<ImageIO> images = imageRequestService.getAllFromDepartment(department);
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ImageIO>> getStudentImage(@RequestParam("matriculation_number") String matriculationNumber) {
        List<ImageIO> images = imageRequestService.getStudentImage(matriculationNumber);
        if (images == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(images, HttpStatus.OK);
    }
}
