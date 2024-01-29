package com.unilorin.attendance_system.datapersistence_api.service;

import com.unilorin.attendance_system.datapersistence_api.entity.ApplicationUser;
import com.unilorin.attendance_system.datapersistence_api.entity.Subject;
import com.unilorin.attendance_system.datapersistence_api.repo.ApplicationUserRepository;
import com.unilorin.attendance_system.datapersistence_api.repo.SubjectRepository;
import com.unilorin.attendance_system.datapersistence_api.utils.ApplicationUserAllParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import java.util.Set;

public class ApplicationUserService{
    private final ApplicationUserRepository userRepository;
    private final SubjectRepository subjectRepository;
    @Autowired
    public ApplicationUserService(ApplicationUserRepository userRepository, SubjectRepository subjectRepository) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }
    public ResponseEntity<ApplicationUserAllParams> findUser(String userId){
        Optional<ApplicationUser> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ApplicationUser user = userOptional.get();
        return new ResponseEntity<>(new ApplicationUserAllParams(user),HttpStatus.OK);
    }
    public ResponseEntity<ApplicationUserAllParams> create(ApplicationUserAllParams appUser){
        Optional<ApplicationUser> userOptional = userRepository.findById(appUser.getId());
        if (userOptional.isEmpty()){
            ApplicationUser applicationUser = appUser.getApplicationUser();
            userRepository.save(applicationUser);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    public ResponseEntity<String> update(String id, String updateType, String update){
        Optional<ApplicationUser> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()){
            return new ResponseEntity<>("user not found",HttpStatus.NOT_FOUND);
        }
        switch(updateType){
            case "password" -> {
                ApplicationUser user = userOptional.get();
                user.setPassword(update);
                userRepository.save(user);
                return new ResponseEntity<>("updated Successfully",HttpStatus.OK);
            }
            case "add_subject" -> {
                ApplicationUser user = userOptional.get();
                Set<Subject> subjectSet = user.getSubjects();
                Optional<Subject> subject = subjectRepository.findById(id);
                if (subject.isEmpty()){
                    return new ResponseEntity<>("subject not found",HttpStatus.NOT_FOUND);
                }
                subjectSet.add(subject.get());
                userRepository.save(user);
                return new ResponseEntity<>("updated successfully",HttpStatus.OK);
            }
            case "remove_subject" ->{
                ApplicationUser user = userOptional.get();
                Set<Subject> subjectSet = user.getSubjects();
                Optional<Subject> subject = subjectRepository.findById(id);
                if (subject.isEmpty()){
                    return new ResponseEntity<>("subject not found",HttpStatus.NOT_FOUND);
                }
                subjectSet.remove(subject.get());
                userRepository.save(user);
                return new ResponseEntity<>("updated successfully",HttpStatus.OK);
            }
            default -> {
                return new ResponseEntity<>("invalid update request",HttpStatus.BAD_REQUEST);
            }
        }
    }
    private ResponseEntity<String> disableAccount(String id){
        Optional<ApplicationUser> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()){
            return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        }
        ApplicationUser user = userOptional.get();
        user.setEnabled(false);
        userRepository.save(user);
        return new ResponseEntity<>("Disabled account",HttpStatus.OK);
    }
    private ResponseEntity<String> enable(String id){
        Optional<ApplicationUser> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()){
            return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        }
        ApplicationUser user = userOptional.get();
        user.setEnabled(true);
        userRepository.save(user);
        return new ResponseEntity<>("Disabled account",HttpStatus.OK);
    }



}
