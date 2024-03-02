package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authorization_service.super_admin.SuperUserService;
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

    /**
     * Creates a new administrator user.
     * This endpoint allows the creation of a new administrator user based on the provided ApplicationUserRequest.
     * The method builds a new user with the specified details and assigns the ROLE_ADMIN role to it.
     * It then attempts to create the user using the applicationUserService.
     * If the user creation is successful, it returns a response
     * indicating that the admin has been added.
     * If the user already exists, it returns a conflict status.
     *
     * @param request The request containing the details of the new administrator user.
     * @return A ResponseEntity indicating the outcome of the operation:
     *         - If the admin is successfully added, returns OK (200) status.
     *         - If the user already exists, returns a CONFLICT (409) status.
     */
    @PostMapping("/add-new-admin")
    public ResponseEntity<String> addNewAdmin(@RequestBody ApplicationUserRequest request) {
        return superUserService.addNewAdmin(request);
    }

    /**
     * Sets a user to the administrator role.
     * This endpoint allows setting an existing user identified by the provided ID to the administrator role.
     * It first attempts to retrieve the user from the database using the provided ID. If the user is found,
     * it attempts to add the ROLE_ADMIN role to the user.
     * If the role addition is successful, it updates the user
     * using the applicationUserService
     * and returns a response indicating that the user has been updated to an admin.
     * If the user is already an admin, it returns a conflict status.
     * If the user is not found, it returns a not found status.
     *
     * @param id The ID of the user to be set as an administrator.
     * @return A ResponseEntity indicating the outcome of the operation:
     *         - If the user is successfully updated to an admin, returns OK (200) status.
     *         - If the user is already an admin, returns a CONFLICT (409) status.
     *         - If the user is not found, returns a NOT_FOUND (404) status.
     */
    @PostMapping("/set-to-admin/{id}")
    public ResponseEntity<String> setToAdmin(@PathVariable String id) {
        return superUserService.setToAdmin(id);
    }

    /**
     * Locks the account associated with the provided user ID.
     * This endpoint locks the account associated with the user ID provided in the path variable.
     * It first attempts to find the user by the provided ID. If the user is found, it checks if the user
     * has the ROLE_SUPER_ADMIN role.
     * If the user does not have the ROLE_SUPER_ADMIN role, it sets the user's
     * enabled status to false, effectively locking the account.
     * It then updates the user using the applicationUserService
     * and returns a response indicating that the account has been successfully locked.
     * If the user is not found, it returns a not found status.
     *
     * @param id The ID of the user whose account is to be locked.
     * @return A ResponseEntity indicating the outcome of the operation:
     *         - If the account is successfully locked, returns OK (200) status.
     *         - If the user is not found, returns a NOT_FOUND (404) status.
     *         - If the user has the ROLE_SUPER_ADMIN role, returns UNAUTHORIZED (401) status.
     */
    @PostMapping("/lock-account/{id}")
    public ResponseEntity<String> lockAccount(@PathVariable String id) {
        return superUserService.lockAccount(id);
    }

    /**
     * Unlocks the account associated with the provided user ID.
     * This endpoint unlocks the account associated with the user ID provided in the path variable.
     * It first attempts to find the user by the provided ID. If the user is found, it checks if the user
     * has the ROLE_SUPER_ADMIN role.
     * If the user does not have the ROLE_SUPER_ADMIN role, it sets the user's
     * enabled status to true, effectively unlocking the account.
     * It then updates the user using the applicationUserService
     * and returns a response indicating that the account has been successfully unlocked.
     * If the user is not found, it returns a not found status.
     *
     * @param id The ID of the user whose account is to be unlocked.
     * @return A ResponseEntity indicating the outcome of the operation:
     *         - If the account is successfully unlocked, returns OK (200) status.
     *         - If the user is not found, returns a NOT_FOUND (404) status.
     *         - If the user has the ROLE_SUPER_ADMIN role, returns UNAUTHORIZED (401) status.
     */
    @PostMapping("/unlock-account/{id}")
    public ResponseEntity<String> unlockAccount(@PathVariable String id) {
        return superUserService.unlockAccount(id);
    }

}
