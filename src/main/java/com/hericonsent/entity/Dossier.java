package com.hericonsent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dossiers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private String titre;

    private String description;

    @Column(name = "reference_cadastrale")
    private String referenceCadastrale;

    @Column(name = "adresse_bien")
    private String adresseBien;

    @Column(nullable = false)
    private String statut = "OUVERT";

    @Column(name = "valeur_estimee")
    private BigDecimal valeurEstimee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notaire_id")
    private User notaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Heritier> heritiers = new ArrayList<>();

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Consentement> consentements = new ArrayList<>();

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

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
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
