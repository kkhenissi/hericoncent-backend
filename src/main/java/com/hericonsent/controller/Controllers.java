package com.hericonsent.controller;

import com.hericonsent.dto.*;
import com.hericonsent.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// ============================================
// AUTH CONTROLLER
// ============================================
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "Inscription, connexion et refresh token")
@RequiredArgsConstructor
class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Créer un compte")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Compte créé avec succès", authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Se connecter")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refreshToken(refreshToken)));
    }
}

// ============================================
// DOSSIER CONTROLLER
// ============================================
@RestController
@RequestMapping("/dossiers")
@Tag(name = "Dossiers", description = "Gestion des biens en indivision")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
class DossierController {

    private final DossierService dossierService;

    @GetMapping
    @Operation(summary = "Lister mes dossiers")
    public ResponseEntity<ApiResponse<List<DossierResponse>>> lister() {
        return ResponseEntity.ok(ApiResponse.ok(dossierService.listerTous()));
    }

    @PostMapping
    @Operation(summary = "Créer un dossier")
    public ResponseEntity<ApiResponse<DossierResponse>> creer(
            @Valid @RequestBody CreateDossierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Dossier créé", dossierService.creer(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un dossier")
    public ResponseEntity<ApiResponse<DossierDetailResponse>> getDetail(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(dossierService.getDetail(id)));
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Changer le statut d'un dossier")
    @PreAuthorize("hasAnyRole('NOTAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponse<DossierResponse>> changerStatut(
            @PathVariable UUID id,
            @RequestParam String statut) {
        return ResponseEntity.ok(ApiResponse.ok(dossierService.changerStatut(id, statut)));
    }
}

// ============================================
// HERITIER CONTROLLER
// ============================================
@RestController
@RequestMapping("/dossiers/{dossierId}/heritiers")
@Tag(name = "Héritiers", description = "Gestion des héritiers d'un dossier")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
class HeritierController {

    private final HeritierService heritierService;

    @GetMapping
    @Operation(summary = "Lister les héritiers d'un dossier")
    public ResponseEntity<ApiResponse<List<HeritierResponse>>> lister(
            @PathVariable UUID dossierId) {
        return ResponseEntity.ok(ApiResponse.ok(heritierService.listerParDossier(dossierId)));
    }

    @PostMapping
    @Operation(summary = "Ajouter un héritier")
    public ResponseEntity<ApiResponse<HeritierResponse>> ajouter(
            @PathVariable UUID dossierId,
            @Valid @RequestBody AddHeritierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Héritier ajouté", heritierService.ajouter(dossierId, request)));
    }

    @DeleteMapping("/{heritierId}")
    @Operation(summary = "Supprimer un héritier")
    @PreAuthorize("hasAnyRole('NOTAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> supprimer(@PathVariable UUID heritierId) {
        heritierService.supprimer(heritierId);
        return ResponseEntity.ok(ApiResponse.ok("Héritier supprimé", null));
    }

    @PatchMapping("/{heritierId}/statut-contact")
    @Operation(summary = "Mettre à jour le statut de contact")
    public ResponseEntity<ApiResponse<HeritierResponse>> mettreAJourContact(
            @PathVariable UUID heritierId,
            @RequestParam String statut) {
        return ResponseEntity.ok(ApiResponse.ok(
                heritierService.mettreAJourStatutContact(heritierId, statut)));
    }
}

// ============================================
// CONSENTEMENT CONTROLLER
// ============================================
@RestController
@Tag(name = "Consentements", description = "Workflow de collecte des consentements")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
class ConsentementController {

    private final ConsentementService consentementService;

    @PostMapping("/dossiers/{dossierId}/consentements")
    @Operation(summary = "Créer une demande de consentement")
    @PreAuthorize("hasAnyRole('NOTAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ConsentementResponse>> creer(
            @PathVariable UUID dossierId,
            @Valid @RequestBody CreateConsentementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Demande de consentement créée",
                        consentementService.creer(dossierId, request)));
    }

    @GetMapping("/dossiers/{dossierId}/consentements")
    @Operation(summary = "Lister les consentements d'un dossier")
    public ResponseEntity<ApiResponse<List<ConsentementResponse>>> lister(
            @PathVariable UUID dossierId) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentementService.listerParDossier(dossierId)));
    }

    @GetMapping("/consentements/{id}")
    @Operation(summary = "Détail d'un consentement")
    public ResponseEntity<ApiResponse<ConsentementResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(consentementService.getById(id)));
    }

    @PostMapping("/consentements/{id}/repondre")
    @Operation(summary = "Répondre à un consentement (utilisateur connecté)")
    public ResponseEntity<ApiResponse<ConsentementResponse>> repondreConnecte(
            @PathVariable UUID id,
            @Valid @RequestBody RepondreConsentementRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentementService.repondreConnecte(id, request)));
    }

    @PostMapping("/consentements/{id}/relancer")
    @Operation(summary = "Relancer les héritiers n'ayant pas répondu")
    @PreAuthorize("hasAnyRole('NOTAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> relancer(@PathVariable UUID id) {
        consentementService.relancer(id);
        return ResponseEntity.ok(ApiResponse.ok("Relances envoyées", null));
    }

    // Route publique (accès par token)
    @PostMapping("/consentements/repondre/token/{token}")
    @Operation(summary = "Répondre via lien sécurisé (sans connexion)")
    public ResponseEntity<ApiResponse<ConsentementResponse>> repondreParToken(
            @PathVariable String token,
            @Valid @RequestBody RepondreConsentementRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentementService.repondreParToken(token, request)));
    }
}
