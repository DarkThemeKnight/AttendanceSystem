package com.backend.FaceRecognition.utils;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableRecords {
    private Set<Data> data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String date;
    }
}
