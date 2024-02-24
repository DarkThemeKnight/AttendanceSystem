package com.backend.FaceRecognition.utils.opencv;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class Beans {
    @Bean
    public LBPHFaceRecognizer faceRecognizer()   {
        try {
            LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
            Resource resource = new ClassPathResource("static/model.xml");
            File file = resource.getFile();
            recognizer.read(file.getPath());
            return recognizer;
        } catch (IOException e) {
            return LBPHFaceRecognizer.create();
        }
    }
    @Bean
    public CascadeClassifier faceCascadeClassifier() throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Resource resource = new ClassPathResource("static/haarcascade_frontalface_default.xml");
        File file = resource.getFile();
        log.info("Cascade classifier load status {}",file.exists());
        return new CascadeClassifier(file.getPath());
    }
}
