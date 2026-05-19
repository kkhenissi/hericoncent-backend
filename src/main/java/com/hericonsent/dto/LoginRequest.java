package com.hericonsent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @Email(message = "Email invalide")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;
}
