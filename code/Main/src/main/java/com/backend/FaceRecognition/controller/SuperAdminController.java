package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authorization_service.super_admin.SuperUserService;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/super-admin")
public class SuperAdminController {

    private final SuperUserService superUserService;

    @Autowired
    public SuperAdminController(SuperUserService superUserService) {
        this.superUserService = superUserService;
    }

    @PostMapping("/add-new-admin")
    public ResponseEntity<String> addNewAdmin(@RequestBody ApplicationUserRequest request) {
        return superUserService.addNewAdmin(request);
    }

    @PostMapping("/set-to-admin/{id}")
    public ResponseEntity<String> setToAdmin(@PathVariable String id) {
        return superUserService.setToAdmin(id);
    }

    @PostMapping("/lock-account/{id}")
    public ResponseEntity<String> lockAccount(@PathVariable String id) {
        return superUserService.lockAccount(id);
    }

    @PostMapping("/unlock-account/{id}")
    public ResponseEntity<String> unlockAccount(@PathVariable String id) {
        return superUserService.unlockAccount(id);
    }
}
