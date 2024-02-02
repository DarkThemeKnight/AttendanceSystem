package com.unilorin.attendance_system.datapersistence_api.controller;

import com.unilorin.attendance_system.datapersistence_api.service.ApplicationUserService;
import com.unilorin.attendance_system.datapersistence_api.utils.ApplicationUserRequest;
import com.unilorin.attendance_system.datapersistence_api.utils.ApplicationUserResponse;
import com.unilorin.attendance_system.datapersistence_api.utils.AuthRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/v1/user/")
public class ApplicationController {
    private final ApplicationUserService applicationUserService;
    @Autowired
    public ApplicationController(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }
    @PostMapping("login")
    public ResponseEntity<ApplicationUserResponse> loginUser(@RequestBody AuthRequest request){
        return applicationUserService.login(request.getUserid(),request.getPassword());
    }
    @GetMapping("get/{id}")
    public ResponseEntity<ApplicationUserResponse> getUser(@PathVariable("id") String id){
        return applicationUserService.findUser(id);
    }
    @PostMapping("create")
    public ResponseEntity<ApplicationUserResponse> create(@RequestBody ApplicationUserRequest user){
        return applicationUserService.create(user);
    }
    @PutMapping("update")
    public ResponseEntity<String> update(@RequestHeader("Authorization")String  bearer,@RequestParam("update_type") String updateType,@RequestParam("update") String update){
        return applicationUserService.update(bearer,updateType,update);
    }

}
