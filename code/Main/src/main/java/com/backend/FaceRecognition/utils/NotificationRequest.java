package com.backend.FaceRecognition.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Data
public class NotificationRequest {
    private String title;
    private String content;
    private String validUntil;
}
