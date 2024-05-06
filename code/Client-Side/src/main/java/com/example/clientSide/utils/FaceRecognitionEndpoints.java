package com.example.clientSide.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@NoArgsConstructor
@AllArgsConstructor
public class FaceRecognitionEndpoints {
    Map<String, String> endpointMap;
    public String getEndpoint(String key){
        return endpointMap.get(key);
    }
}
