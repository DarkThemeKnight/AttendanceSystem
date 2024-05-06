package com.example.clientSide.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;
@NoArgsConstructor
@Data
public class  ScheduleSetupRequest {
    private String updateType;
    List<CustomRequest> data;
    @Data
    @NoArgsConstructor
    public static class CustomRequest{
        private Integer id;//required for update
        private String courseCode;
        private String duration;
        private String time;
        private DayOfWeek dayOfWeek;
    }
}
