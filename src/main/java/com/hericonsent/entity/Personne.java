package com.hericonsent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    // Family Tree Fields
    @Column(name = "gender")
    private String gender; // "male" or "female"

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "death_year")
    private Integer deathYear;

    @Column(name = "profession")
    private String profession;

    @Column(name = "city")
    private String city;

    @Column(name = "spouse_id")
    private UUID spouseId;

    @Column(name = "parent_ids", columnDefinition = "TEXT")
    private String parentIds; // JSON array stored as string: "['id1', 'id2']"

    @Column(name = "photo_initials")
    private String photoInitials;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = OffsetDateTime.now(); }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
