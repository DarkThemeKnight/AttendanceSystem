package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSetupPolicy {
   @Id
   private String code;
   @Column(name = "subject_id")
   private String subjectId;
   private int duration;
   private LocalDate attendanceDate;
   private LocalDateTime attendanceDateTime;
}
