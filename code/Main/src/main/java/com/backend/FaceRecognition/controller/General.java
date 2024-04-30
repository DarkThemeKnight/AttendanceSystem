package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.Notification;
import com.backend.FaceRecognition.repository.NotificationRepository;
import com.backend.FaceRecognition.services.extras.ScheduleService;
import com.backend.FaceRecognition.utils.NotificationResponse;
import com.backend.FaceRecognition.utils.ScheduleSetupRequest;
import com.backend.FaceRecognition.utils.ScheduleSetupResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/general")
public class General {
    private final NotificationRepository notificationRepository;
    private final ScheduleService scheduleService;
    public General(NotificationRepository notificationRepository, ScheduleService scheduleService) {
        this.notificationRepository = notificationRepository;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/get-notifications")
    private ResponseEntity<NotificationResponse> getMyNotifications(){
        List<Notification> notifications = notificationRepository.findAll();
        List<NotificationResponse.CustomData> data = notifications.stream()
                .map(notification -> new NotificationResponse.CustomData(notification.getTitle(),notification.getContent()))
                .toList();
        NotificationResponse response = new NotificationResponse("successful",data);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/get-schedule")
    private ResponseEntity<ScheduleSetupResponse> getMySchedule(@RequestHeader("Authorization") String bearer){
        return scheduleService.fetch(bearer);
    }
    @PostMapping("/add-schedule")
    private ResponseEntity<ScheduleSetupResponse> addSchedule(@RequestBody ScheduleSetupRequest scheduleRequest, @RequestHeader("Authorization") String auth){
        return scheduleService.setupMySchedule(scheduleRequest,auth);
    }


}
