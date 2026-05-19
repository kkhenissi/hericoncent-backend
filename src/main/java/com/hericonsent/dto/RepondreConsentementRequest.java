package com.hericonsent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RepondreConsentementRequest {
    @NotBlank
    @Pattern(regexp = "ACCEPTE|REJETE|DELEGUE")
    private String reponse;
    private String commentaire;
    private boolean signatureRequise = false;
}
