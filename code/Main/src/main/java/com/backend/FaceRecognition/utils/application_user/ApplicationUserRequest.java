package com.backend.FaceRecognition.utils.application_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserRequest {
    private String id;
    private String firstname;
    private String lastname;
    private String middleName;
    private String schoolEmail;
    private String password;
}
