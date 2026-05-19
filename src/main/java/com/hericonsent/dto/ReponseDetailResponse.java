package com.hericonsent.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Data @Builder
public class ReponseDetailResponse {
    private UUID id;
    private UUID heritierId;
    private String heritierNom;
    private String reponse;
    private String commentaire;
    private OffsetDateTime reponduLe;
    private boolean signe;
}



