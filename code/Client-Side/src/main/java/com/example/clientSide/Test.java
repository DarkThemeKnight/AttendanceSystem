package com.example.clientSide;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

public class Test {
    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoCapture capture= new VideoCapture(0);
        if (!capture.isOpened()) {
            System.err.println("Failed to open the camera");
            return;
        }


    }
}
