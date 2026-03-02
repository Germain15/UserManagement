package com.imediaconsulting.userManagement.service;

import com.imediaconsulting.userManagement.dto.CreateUserRequest;
import com.imediaconsulting.userManagement.dto.UserResponse;
import com.imediaconsulting.userManagement.exception.EmailAlreadyExistsException;
import com.imediaconsulting.userManagement.model.User;
import com.imediaconsulting.userManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        // Vérification si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Construction de l'objet User
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        // Sauvegarde en base
        User saved = userRepository.save(user);

        return toResponse(saved);
    } // <-- Accolade de fin de méthode bien présente ici

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}