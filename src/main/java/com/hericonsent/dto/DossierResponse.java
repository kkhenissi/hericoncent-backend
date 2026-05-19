package com.hericonsent.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class DossierResponse {
    private UUID id;
    private String reference;
    private String titre;
    private String description;
    private String referenceCadastrale;
    private String adresseBien;
    private String statut;
    private BigDecimal valeurEstimee;
    private int nombreHeritiers;
    private int nombreConsentements;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
