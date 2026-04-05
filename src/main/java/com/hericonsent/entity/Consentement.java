package com.hericonsent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consentements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Consentement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private Dossier dossier;

    @Column(nullable = false)
    private String titre;

    private String description;

    @Column(name = "type_action", nullable = false)
    private String typeAction;  // VENTE, PARTAGE, DONATION, MANDAT, AUTRE

    @Column(nullable = false)
    private String statut = "EN_ATTENTE";
    // EN_ATTENTE, PARTIEL, VALIDE, REJETE, EXPIRE

    @Column(name = "seuil_accord")
    private BigDecimal seuilAccord = new BigDecimal("100.0");

    @Column(name = "expire_le")
    private OffsetDateTime expireLe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par")
    private User creePar;

    @OneToMany(mappedBy = "consentement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConsentementReponse> reponses = new ArrayList<>();

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
