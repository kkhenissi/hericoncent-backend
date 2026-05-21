package com.hericonsent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddHeritierRequest {
    @NotBlank
    private String nom;
    @NotBlank
    private String prenom;
    @Email
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private String adresse;
    private String gender; // "male" or "female"
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal part;
    private boolean validated = false;

    @JsonProperty("isHeir")
    private boolean isHeir = false;

    private String role = "HERITIER";
}
