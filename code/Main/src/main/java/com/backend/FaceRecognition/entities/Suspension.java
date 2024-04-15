package com.backend.FaceRecognition.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
public class Suspension {
    @Id
    private Long id;
    private String studentId;
    private String subjectId;
}

