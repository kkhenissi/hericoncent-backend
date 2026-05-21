package com.hericonsent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class HeritierResponse {
    private UUID id;
    private UUID personneId;
    private String nomComplet;
    private String email;
    private String telephone;
    private BigDecimal part;
    private String role;
    private boolean validated;

    @JsonProperty("isHeir")
    private boolean isHeir;

    private String statutContact;
    private boolean identityVerified;
    private String reponseConsentement;
}
