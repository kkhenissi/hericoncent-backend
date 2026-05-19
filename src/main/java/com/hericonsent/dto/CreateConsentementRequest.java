package com.hericonsent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class CreateConsentementRequest {
    @NotBlank
    private String titre;
    private String description;
    @NotBlank
    private String typeAction;  // VENTE, PARTAGE, DONATION, MANDAT, AUTRE
    private BigDecimal seuilAccord = new BigDecimal("100.0");
    private OffsetDateTime expireLe;
}
