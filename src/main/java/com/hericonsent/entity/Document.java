package com.hericonsent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private Dossier dossier;

    @Column(nullable = false)
    private String nom;

    @Column(name = "type_doc", nullable = false)
    private String typeDoc;  // ACTE_PROPRIETE, PIECE_IDENTITE, ACTE_NAISSANCE, PROCURATION, AUTRE

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "mime_type")
    private String mimeType;

    private Long taille;
    private String checksum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_par")
    private User uploadPar;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;

    @PrePersist
    public void prePersist() { uploadedAt = OffsetDateTime.now(); }
}
