package com.backend.FaceRecognition.utils.application_user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUserRequest {
    private String id;
    private String firstname;
    private String lastname;
    private String middleName;
    private String schoolEmail;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    private String faculty;
    private String department;
    private String specialAccommodations;
}
