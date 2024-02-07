package com.backend.FaceRecognition.utils.opencv;

import org.opencv.core.Core;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Beans {
    @Bean
    public void loadLibrary(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
