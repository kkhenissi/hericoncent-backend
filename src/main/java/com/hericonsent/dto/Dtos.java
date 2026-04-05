package com.hericonsent.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// ============================================
// AUTH DTOs
// ============================================

@Data
public class LoginRequest {
    @Email(message = "Email invalide")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;
}

@Data
public class RegisterRequest {
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank private String nom;
    @NotBlank private String prenom;
    private String telephone;
    private String role = "ROLE_HEIR";
}

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String email;
    private String role;
    private UUID userId;
}

// ============================================
// DOSSIER DTOs
// ============================================

@Data
public class CreateDossierRequest {
    @NotBlank private String titre;
    private String description;
    private String referenceCadastrale;
    private String adresseBien;
    private BigDecimal valeurEstimee;
    private UUID notaireId;
}

@Data @Builder
public class DossierResponse {
    private UUID id;
    private String reference;
    private String titre;
    private String description;
    private String referenceCadastrale;
    private String adresseBien;
    private String statut;
    private BigDecimal valeurEstimee;
    private int nombreHeritiers;
    private int nombreConsentements;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

@Data @Builder
public class DossierDetailResponse {
    private UUID id;
    private String reference;
    private String titre;
    private String description;
    private String referenceCadastrale;
    private String adresseBien;
    private String statut;
    private BigDecimal valeurEstimee;
    private List<HeritierResponse> heritiers;
    private List<ConsentementResponse> consentements;
    private List<DocumentResponse> documents;
    private OffsetDateTime createdAt;
}

// ============================================
// HERITIER DTOs
// ============================================

@Data
public class AddHeritierRequest {
    @NotBlank private String nom;
    @NotBlank private String prenom;
    @Email private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private String adresse;
    @NotNull
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal part;
    private String role = "HERITIER";
}

@Data @Builder
public class HeritierResponse {
    private UUID id;
    private UUID personneId;
    private String nomComplet;
    private String email;
    private String telephone;
    private BigDecimal part;
    private String role;
    private String statutContact;
    private boolean identityVerified;
    private String reponseConsentement;
}

// ============================================
// CONSENTEMENT DTOs
// ============================================

@Data
public class CreateConsentementRequest {
    @NotBlank private String titre;
    private String description;
    @NotBlank private String typeAction;  // VENTE, PARTAGE, DONATION, MANDAT, AUTRE
    private BigDecimal seuilAccord = new BigDecimal("100.0");
    private OffsetDateTime expireLe;
}

@Data @Builder
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
}

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

// ============================================
// REPONSE / VOTE DTOs
// ============================================

@Data
public class RepondreConsentementRequest {
    @NotBlank
    @Pattern(regexp = "ACCEPTE|REJETE|DELEGUE")
    private String reponse;
    private String commentaire;
    private boolean signatureRequise = false;
}

// ============================================
// DOCUMENT DTOs
// ============================================

@Data @Builder
public class DocumentResponse {
    private UUID id;
    private String nom;
    private String typeDoc;
    private String mimeType;
    private Long taille;
    private OffsetDateTime uploadedAt;
    private String downloadUrl;
}

// ============================================
// AUDIT DTOs
// ============================================

@Data @Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private String entiteType;
    private UUID entiteId;
    private UUID acteurId;
    private OffsetDateTime createdAt;
}

// ============================================
// API RESPONSE WRAPPER
// ============================================

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }
}
