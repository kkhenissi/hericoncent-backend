package com.hericonsent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consentement_reponses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsentementReponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consentement_id", nullable = false)
    private Consentement consentement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "heritier_id", nullable = false)
    private Heritier heritier;

    private String reponse;         // ACCEPTE, REJETE, DELEGUE, EN_ATTENTE
    private String commentaire;

    @Column(name = "token_acces", unique = true)
    private String tokenAcces;

    @Column(name = "token_expire_le")
    private OffsetDateTime tokenExpireLe;

    @Column(name = "repondu_le")
    private OffsetDateTime reponduLe;

    @Column(name = "signature_id")
    private UUID signatureId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = OffsetDateTime.now(); }
}
