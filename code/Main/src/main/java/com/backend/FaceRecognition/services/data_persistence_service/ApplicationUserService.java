package com.backend.FaceRecognition.services.data_persistence_service;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.repository.ApplicationUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ApplicationUserService {
    private final ApplicationUserRepository applicationUserRepository;
    public ApplicationUserService(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }
    public Optional<ApplicationUser> findUser(String userId){
        return applicationUserRepository.findById(userId);
    }
    /**
     * Creates a new application user.
     * This method attempts to create a new application user in the system.
     * It checks if the user already exists
     * based on the provided user ID. If the user already exists, a conflict status is returned.
     * If the user doesn't exist, it is saved to the repository,
     * and a success status is returned.
     *
     * @param applicationUser The ApplicationUser object representing the user to be created.
     * @return A ResponseEntity indicating the result of the operation.
     *         If the user already exists, a conflict status is returned.
     *         If the user is successfully created, an OK status is returned.
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
     * Updates an existing application user.
     * This method attempts to update an existing application user in the system based on the provided user ID.
     * If the user exists, it updates the user in the repository.
     * If the user doesn't exist, a not found status
     * is returned.
     *
     * @param appUser The ApplicationUser object representing the user to be updated.
     */
    public void update(ApplicationUser appUser) {
        // Check if the user exists
        Optional<ApplicationUser> user = applicationUserRepository.findById(appUser.getId());
        if (user.isPresent()) {
            // User exists, update it in the repository
            applicationUserRepository.save(appUser);
            // Return success status
            new ResponseEntity<>(HttpStatus.OK);
            return;
        }
        // User doesn't exist, return not found status
        new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }





}