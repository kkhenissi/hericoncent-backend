package com.hericonsent.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ConsentementResponse {
    private UUID id;
    private String titre;
    private String description;
    private String typeAction;
    private String statut;
    private BigDecimal seuilAccord;
    private OffsetDateTime expireLe;
    private int totalHeritiers;
    private int reponsesAcceptees;
    private int reponsesRejetees;
    private int reponsesEnAttente;
    private double progressPercent;
    private OffsetDateTime createdAt;
    private List<ReponseDetailResponse> reponses;
    private Boolean dejaRepondu;        // non-null uniquement sur GET /repondre/token/{token}
    private String reponseActuelle;     // ACCEPTE / REJETE / EN_ATTENTE si déjà répondu
    private BigDecimal valeurEstimee;   // valeur estimée du bien (peut être null)
    private BigDecimal partHeritier;    // part de cet héritier (0.0 – 1.0)
}
