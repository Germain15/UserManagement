package com.imediaconsulting.userManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    // La Regex ci-dessous accepte les caractères classiques avant le @ et impose @gmail.com à la fin
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", 
        message = "Seul les adresses de cette forme: (example@gmail.com) sont acceptés"
    )
    private String email;
}