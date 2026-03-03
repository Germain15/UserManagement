/**
 * Exposition HTTP des fonctionnalités utilisateur.
 * Ce contrôleur ne contient aucune logique — il reçoit la requête, délègue au service, retourne la réponse.
 */
package com.imediaconsulting.userManagement.controller;

import com.imediaconsulting.userManagement.dto.CreateUserRequest;
import com.imediaconsulting.userManagement.dto.UserResponse;
import com.imediaconsulting.userManagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}