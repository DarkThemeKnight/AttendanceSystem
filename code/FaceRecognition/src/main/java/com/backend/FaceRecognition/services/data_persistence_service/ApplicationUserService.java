package com.backend.FaceRecognition.services.data_persistence_service;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.repository.ApplicationUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ApplicationUserService {
    private final ApplicationUserRepository applicationUserRepository;

    @Autowired
    public ApplicationUserService(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }
    public Optional<ApplicationUser> findUser(String userId){
        return applicationUserRepository.findById(userId);
    }
    /**
     * Creates a new ApplicationUser if it doesn't already exist.
     *
     * @param applicationUser The ApplicationUser object to be created.
     * @return ResponseEntity<Void> An HTTP response entity indicating the outcome of the operation.
     *         - HttpStatus.CONFLICT if the user with the provided ID already exists.
     *         - HttpStatus.OK if the user is successfully created.
     */
    public ResponseEntity<Void> create(ApplicationUser applicationUser) {
        // Check if the user already exists
        Optional<ApplicationUser> userOptional = applicationUserRepository.findById(applicationUser.getId());
        if (userOptional.isPresent()) {
            // User already exists, return conflict status
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // User doesn't exist, save it to the repository
        applicationUserRepository.save(applicationUser);

        // Return success status
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Updates an existing ApplicationUser if it exists.
     *
     * @param appUser The ApplicationUser object to be updated.
     * @return ResponseEntity<Void> An HTTP response entity indicating the outcome of the operation.
     *         - HttpStatus.OK if the user is successfully updated.
     *         - HttpStatus.NOT_FOUND if the user with the provided ID does not exist.
     */
    public ResponseEntity<Void> update(ApplicationUser appUser) {
        // Check if the user exists
        Optional<ApplicationUser> user = applicationUserRepository.findById(appUser.getId());
        if (user.isPresent()) {
            // User exists, update it in the repository
            applicationUserRepository.save(appUser);
            // Return success status
            return new ResponseEntity<>(HttpStatus.OK);
        }
        // User doesn't exist, return not found status
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }




}