package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String courseCode;
    private String courseTitle;
    private String duration;
    private String time;
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    private String userId;
}
