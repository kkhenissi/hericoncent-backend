package com.hericonsent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "personnes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Personne {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String email;
    private String telephone;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    private String adresse;
    private String nationalite;

    @Column(name = "identity_verified")
    private boolean identityVerified = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = OffsetDateTime.now(); }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
