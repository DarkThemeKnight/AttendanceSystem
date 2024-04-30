package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.authorization_service.super_admin.SuperUserService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/super-admin")
public class SuperAdminController {

    private final SuperUserService superUserService;

    public SuperAdminController(SuperUserService superUserService) {
        this.superUserService = superUserService;
    }

    @PostMapping("/add-new-admin")
    public ResponseEntity<Response> addNewAdmin(@RequestBody ApplicationUserRequest request) {
        return superUserService.addNewAdmin(request);
    }

    @PostMapping("/set-to-admin/{id}")
    public ResponseEntity<Response> setToAdmin(@PathVariable String id) {
        return superUserService.setToAdmin(id);
    }

    @GetMapping("/{code}")
    public ResponseEntity<String> getAll(@PathVariable("code") String code) {
        return superUserService.getAll(code.toLowerCase());
    }
    @GetMapping("/get-user")
    public ResponseEntity<ApplicationUser> getUser(@RequestParam("id") String id){
        return superUserService.getUser(id);
    }
}
