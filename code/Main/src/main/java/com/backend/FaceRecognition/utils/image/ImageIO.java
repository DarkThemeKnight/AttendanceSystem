package com.backend.FaceRecognition.utils.image;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ImageIO {
    @JsonProperty("matriculation_number")
    String matriculationNumber;
    @JsonProperty("images")
    byte[] imBytes; // Change the type to String for Base64 encoded images
}