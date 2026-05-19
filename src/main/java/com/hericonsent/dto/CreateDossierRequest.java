package com.hericonsent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateDossierRequest {
    @NotBlank
    private String titre;
    private String description;
    private String referenceCadastrale;
    private String adresseBien;
    private BigDecimal valeurEstimee;
    private UUID notaireId;
}
