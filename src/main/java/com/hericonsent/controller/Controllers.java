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
    private final FamilyTreeService familyTreeService;

    @GetMapping
    @Operation(summary = "Lister mes dossiers")
    public ResponseEntity<ApiResponse<List<DossierResponse>>> lister(
            @RequestParam(required = false) UUID notaireId) {
        return ResponseEntity.ok(ApiResponse.ok(dossierService.listerTous(notaireId)));
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

    @GetMapping("/{dossierId}/family-tree")
    @Operation(summary = "Récupérer les membres de la famille d'un dossier")
    public ResponseEntity<ApiResponse<List<FamilyMemberResponse>>> getFamilyTree(
            @PathVariable UUID dossierId) {
        return ResponseEntity.ok(ApiResponse.ok(familyTreeService.getMembersByDossier(dossierId)));
    }

    @PostMapping("/{dossierId}/family-tree/members")
    @Operation(summary = "Créer un nouveau membre de la famille pour un dossier")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> createFamilyMember(
            @PathVariable UUID dossierId,
            @Valid @RequestBody CreateFamilyMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Membre créé", familyTreeService.createMember(request)));
    }

    @PutMapping("/{dossierId}/family-tree/members/{memberId}")
    @Operation(summary = "Mettre à jour un membre de la famille")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> updateFamilyMember(
            @PathVariable UUID dossierId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateFamilyMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Membre mis à jour",
                familyTreeService.updateMember(dossierId, memberId, request)));
    }

    @DeleteMapping("/{dossierId}/family-tree/members/{memberId}")
    @Operation(summary = "Supprimer un membre de la famille")
    public ResponseEntity<ApiResponse<Void>> deleteFamilyMember(
            @PathVariable UUID dossierId,
            @PathVariable UUID memberId) {
        familyTreeService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.ok("Membre supprimé", null));
    }

    @PostMapping("/{dossierId}/family-tree/link-couple")
    @Operation(summary = "Lier deux personnes comme couple")
    public ResponseEntity<ApiResponse<Void>> linkCouple(
            @PathVariable UUID dossierId,
            @RequestParam UUID maleId,
            @RequestParam UUID femaleId) {
        familyTreeService.linkCouple(maleId, femaleId);
        return ResponseEntity.ok(ApiResponse.ok("Couple créé", null));
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

    @PatchMapping("/{heritierId}")
    @Operation(summary = "Mettre à jour l'email ou la part d'un héritier")
    @PreAuthorize("hasAnyRole('NOTAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponse<HeritierResponse>> mettreAJour(
            @PathVariable UUID dossierId,
            @PathVariable UUID heritierId,
            @Valid @RequestBody UpdateHeritierRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Héritier mis à jour",
                heritierService.mettreAJour(heritierId, request)));
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

    // Routes publiques (accès par token — sans connexion)
    @GetMapping("/consentements/repondre/token/{token}")
    @Operation(summary = "Consulter un consentement via lien sécurisé (sans connexion)")
    public ResponseEntity<ApiResponse<ConsentementResponse>> getByToken(
            @PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentementService.getByToken(token)));
    }

    @PostMapping("/consentements/repondre/token/{token}")
    @Operation(summary = "Répondre via lien sécurisé (sans connexion)")
    public ResponseEntity<ApiResponse<ConsentementResponse>> repondreParToken(
            @PathVariable String token,
            @Valid @RequestBody RepondreConsentementRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                consentementService.repondreParToken(token, request)));
    }
}

// ============================================
// FAMILY TREE CONTROLLER
// ============================================
@RestController
@RequestMapping("/family-tree")
@Tag(name = "Arbre Généalogique", description = "Gestion de l'arbre généalogique de la famille")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
class FamilyTreeController {

    private final FamilyTreeService familyTreeService;

    @GetMapping
    @Operation(summary = "Récupérer tous les membres de la famille")
    public ResponseEntity<ApiResponse<List<FamilyMemberResponse>>> getAllMembers() {
        return ResponseEntity.ok(ApiResponse.ok(familyTreeService.getAllMembers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un membre de la famille par ID")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> getMemberById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(familyTreeService.getMemberById(id)));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau membre de la famille")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> createMember(
            @Valid @RequestBody CreateFamilyMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Membre créé", familyTreeService.createMember(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un membre de la famille")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> updateMember(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFamilyMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Membre mis à jour", 
                familyTreeService.updateMember(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un membre de la famille")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable UUID id) {
        familyTreeService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.ok("Membre supprimé", null));
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "Récupérer les enfants d'un membre")
    public ResponseEntity<ApiResponse<List<FamilyMemberResponse>>> getChildren(
            @PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.ok(familyTreeService.getChildren(parentId)));
    }

    @GetMapping("/roots")
    @Operation(summary = "Récupérer les racines de l'arbre (membres sans parents)")
    public ResponseEntity<ApiResponse<List<FamilyMemberResponse>>> getRoots() {
        return ResponseEntity.ok(ApiResponse.ok(familyTreeService.getRoots()));
    }

    @GetMapping("/search")
    @Operation(summary = "Chercher un membre de la famille")
    public ResponseEntity<ApiResponse<List<FamilyMemberResponse>>> searchMembers(
            @RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok(familyTreeService.searchMembers(query)));
    }
}
