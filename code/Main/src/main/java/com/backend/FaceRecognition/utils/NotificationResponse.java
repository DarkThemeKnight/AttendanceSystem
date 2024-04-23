package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private String message;
    private List<CustomData> data;
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomData{
        private String title;
        private String content;
    }


}
