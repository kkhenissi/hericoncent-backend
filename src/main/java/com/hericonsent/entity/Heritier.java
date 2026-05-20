package com.hericonsent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "heritiers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Heritier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "personne_id", nullable = false)
    private Personne personne;

    @Column(nullable = false)
    private BigDecimal part = BigDecimal.ZERO;

    @Column(nullable = false)
    private String role = "HERITIER";

    @Column(nullable = false)
    private boolean validated = false;

    @Column(name = "est_representant")
    private boolean estRepresentant = false;

    @Column(name = "represente_par")
    private UUID representePar;

    @Column(name = "statut_contact")
    private String statutContact = "NON_CONTACTE";

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = OffsetDateTime.now(); }
}
