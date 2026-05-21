package com.hericonsent.service;

import com.hericonsent.dto.ReponseDetailResponse;
import com.hericonsent.dto.*;
import com.hericonsent.entity.*;
import com.hericonsent.exception.ResourceNotFoundException;
import com.hericonsent.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentementService {

    private final ConsentementRepository consentementRepository;
    private final ConsentementReponseRepository reponseRepository;
    private final DossierRepository dossierRepository;
    private final HeritierRepository heritierRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Value("${app.consent-token-expiry-hours:72}")
    private int tokenExpiryHours;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String baseUrl;

    // ============================================
    // CRÉER UN CONSENTEMENT
    // ============================================
    @Transactional
    public ConsentementResponse creer(UUID dossierId, CreateConsentementRequest request) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + dossierId));

        User currentUser = getCurrentUser();

        Consentement consentement = Consentement.builder()
                .dossier(dossier)
                .titre(request.getTitre())
                .description(request.getDescription())
                .typeAction(request.getTypeAction())
                .statut("EN_ATTENTE")
                .seuilAccord(request.getSeuilAccord())
                .expireLe(request.getExpireLe())
                .creePar(currentUser)
                .build();

        consentement = consentementRepository.save(consentement);

        // Créer une réponse EN_ATTENTE uniquement pour les membres marqués comme héritiers
        // (case cochée dans l'arbre généalogique, et non décédés)
        List<Heritier> heritiers = heritierRepository.findByDossierId(dossierId).stream()
                .filter(Heritier::isHeir)
                .filter(h -> h.getPersonne() != null && h.getPersonne().getDeathYear() == null)
                .collect(Collectors.toList());
        List<ConsentementReponse> reponses = new ArrayList<>();

        for (Heritier h : heritiers) {
            String token = UUID.randomUUID().toString();
            ConsentementReponse reponse = ConsentementReponse.builder()
                    .consentement(consentement)
                    .heritier(h)
                    .reponse("EN_ATTENTE")
                    .tokenAcces(token)
                    .tokenExpireLe(OffsetDateTime.now().plusHours(tokenExpiryHours))
                    .build();
            reponses.add(reponseRepository.save(reponse));

            // Envoyer notification si email disponible
            if (h.getPersonne().getEmail() != null) {
                notificationService.notifierNouveauConsentement(
                        h.getPersonne().getEmail(),
                        h.getPersonne().getNomComplet(),
                        dossier.getTitre(),
                        token,
                        baseUrl.split(",")[0]
                );
            }
        }

        auditService.log("CREATION_CONSENTEMENT", "CONSENTEMENT", consentement.getId(),
                currentUser.getId(),
                Map.of("dossier", dossierId.toString(), "type", request.getTypeAction()));

        log.info("Consentement créé pour dossier {} avec {} héritiers à notifier",
                dossierId, heritiers.size());

        return toResponse(consentement, reponses);
    }

    // ============================================
    // RÉCUPÉRER UN CONSENTEMENT
    // ============================================
    @Transactional(readOnly = true)
    public ConsentementResponse getById(UUID id) {
        Consentement c = consentementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consentement introuvable : " + id));
        List<ConsentementReponse> reponses = reponseRepository.findByConsentementId(id);
        return toResponse(c, reponses);
    }

    @Transactional(readOnly = true)
    public List<ConsentementResponse> listerParDossier(UUID dossierId) {
        return consentementRepository.findByDossierId(dossierId)
                .stream()
                .map(c -> toResponse(c, reponseRepository.findByConsentementId(c.getId())))
                .collect(Collectors.toList());
    }

    // ============================================
    // RÉPONDRE PAR TOKEN (lien public)
    // ============================================
    @Transactional
    public ConsentementResponse repondreParToken(String token, RepondreConsentementRequest request) {
        ConsentementReponse reponse = reponseRepository.findByTokenAcces(token)
                .orElseThrow(() -> new ResourceNotFoundException("Lien de consentement invalide"));

        if (reponse.getTokenExpireLe() != null
                && reponse.getTokenExpireLe().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Ce lien de consentement a expiré");
        }

        if (reponse.getReponduLe() != null) {
            throw new IllegalStateException("Vous avez déjà répondu à cette demande");
        }

        reponse.setReponse(request.getReponse());
        reponse.setCommentaire(request.getCommentaire());
        reponse.setReponduLe(OffsetDateTime.now());
        reponseRepository.save(reponse);

        auditService.log("REPONSE_CONSENTEMENT", "CONSENTEMENT_REPONSE",
                reponse.getId(), null,
                Map.of("reponse", request.getReponse(),
                        "heritier", reponse.getHeritier().getPersonne().getNomComplet()));

        // Recalculer le statut global du consentement
        Consentement consentement = reponse.getConsentement();
        recalculerStatut(consentement);

        log.info("Réponse '{}' enregistrée pour héritier {}",
                request.getReponse(),
                reponse.getHeritier().getPersonne().getNomComplet());

        return toResponse(consentement, reponseRepository.findByConsentementId(consentement.getId()));
    }

    // ============================================
    // RÉPONDRE EN ÉTANT CONNECTÉ
    // ============================================
    @Transactional
    public ConsentementResponse repondreConnecte(UUID consentementId,
                                                  RepondreConsentementRequest request) {
        User currentUser = getCurrentUser();
        Consentement consentement = consentementRepository.findById(consentementId)
                .orElseThrow(() -> new ResourceNotFoundException("Consentement introuvable"));

        // Trouver la réponse de cet héritier
        Heritier heritier = heritierRepository
                .findByDossierIdAndUserId(consentement.getDossier().getId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vous n'êtes pas héritier de ce dossier"));

        ConsentementReponse reponse = reponseRepository
                .findByConsentementIdAndHeritierID(consentementId, heritier.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Réponse introuvable"));

        if (reponse.getReponduLe() != null) {
            throw new IllegalStateException("Vous avez déjà répondu à cette demande");
        }

        reponse.setReponse(request.getReponse());
        reponse.setCommentaire(request.getCommentaire());
        reponse.setReponduLe(OffsetDateTime.now());
        reponseRepository.save(reponse);

        recalculerStatut(consentement);

        auditService.log("REPONSE_CONSENTEMENT", "CONSENTEMENT_REPONSE",
                reponse.getId(), currentUser.getId(),
                Map.of("reponse", request.getReponse()));

        return toResponse(consentement, reponseRepository.findByConsentementId(consentementId));
    }

    // ============================================
    // RELANCER LES HÉRITIERS N'AYANT PAS RÉPONDU
    // ============================================
    @Transactional
    public void relancer(UUID consentementId) {
        Consentement consentement = consentementRepository.findById(consentementId)
                .orElseThrow(() -> new ResourceNotFoundException("Consentement introuvable"));

        List<ConsentementReponse> enAttente = reponseRepository
                .findByConsentementId(consentementId)
                .stream()
                .filter(r -> "EN_ATTENTE".equals(r.getReponse()))
                .collect(Collectors.toList());

        for (ConsentementReponse r : enAttente) {
            String email = r.getHeritier().getPersonne().getEmail();
            if (email != null && r.getTokenAcces() != null) {
                notificationService.notifierRelance(
                        email,
                        r.getHeritier().getPersonne().getNomComplet(),
                        consentement.getTitre(),
                        r.getTokenAcces(),
                        baseUrl.split(",")[0]
                );
            }
        }

        auditService.log("RELANCE_CONSENTEMENT", "CONSENTEMENT",
                consentementId, getCurrentUser().getId(),
                Map.of("nb_relances", String.valueOf(enAttente.size())));

        log.info("{} relances envoyées pour consentement {}", enAttente.size(), consentementId);
    }

    // ============================================
    // LOGIQUE MÉTIER : recalcul du statut
    // ============================================
    private void recalculerStatut(Consentement consentement) {
        List<ConsentementReponse> toutes = reponseRepository
                .findByConsentementId(consentement.getId());

        long total   = toutes.size();
        long accepte = toutes.stream().filter(r -> "ACCEPTE".equals(r.getReponse())).count();
        long rejete  = toutes.stream().filter(r -> "REJETE".equals(r.getReponse())).count();
        long attente = toutes.stream().filter(r -> "EN_ATTENTE".equals(r.getReponse())).count();

        // Calcul des parts acceptées
        BigDecimal partsAcceptees = toutes.stream()
                .filter(r -> "ACCEPTE".equals(r.getReponse()))
                .map(r -> r.getHeritier().getPart())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(new BigDecimal("100"));

        String nouveauStatut;
        if (rejete > 0) {
            nouveauStatut = "REJETE";
        } else if (attente == 0 && partsAcceptees.compareTo(consentement.getSeuilAccord()) >= 0) {
            nouveauStatut = "VALIDE";
            // Notifier tous les héritiers
            notifierValidation(consentement, toutes);
        } else if (accepte > 0) {
            nouveauStatut = "PARTIEL";
        } else {
            nouveauStatut = "EN_ATTENTE";
        }

        if (!nouveauStatut.equals(consentement.getStatut())) {
            consentement.setStatut(nouveauStatut);
            consentementRepository.save(consentement);
            log.info("Consentement {} → statut : {}", consentement.getId(), nouveauStatut);
        }
    }

    private void notifierValidation(Consentement consentement, List<ConsentementReponse> reponses) {
        for (ConsentementReponse r : reponses) {
            String email = r.getHeritier().getPersonne().getEmail();
            if (email != null) {
                notificationService.notifierConsentementValide(
                        email,
                        r.getHeritier().getPersonne().getNomComplet(),
                        consentement.getTitre()
                );
            }
        }
    }

    // ============================================
    // MAPPERS
    // ============================================
    private ConsentementResponse toResponse(Consentement c, List<ConsentementReponse> reponses) {
        int total   = reponses.size();
        int accepte = (int) reponses.stream().filter(r -> "ACCEPTE".equals(r.getReponse())).count();
        int rejete  = (int) reponses.stream().filter(r -> "REJETE".equals(r.getReponse())).count();
        int attente = (int) reponses.stream().filter(r -> "EN_ATTENTE".equals(r.getReponse())).count();

        double progress = total > 0
                ? ((double)(total - attente) / total) * 100
                : 0;

        List<ReponseDetailResponse> details = reponses.stream()
                .map(r -> ReponseDetailResponse.builder()
                        .id(r.getId())
                        .heritierId(r.getHeritier().getId())
                        .heritierNom(r.getHeritier().getPersonne().getNomComplet())
                        .reponse(r.getReponse())
                        .commentaire(r.getCommentaire())
                        .reponduLe(r.getReponduLe())
                        .signe(r.getSignatureId() != null)
                        .build())
                .collect(Collectors.toList());

        return ConsentementResponse.builder()
                .id(c.getId())
                .titre(c.getTitre())
                .description(c.getDescription())
                .typeAction(c.getTypeAction())
                .statut(c.getStatut())
                .seuilAccord(c.getSeuilAccord())
                .expireLe(c.getExpireLe())
                .totalHeritiers(total)
                .reponsesAcceptees(accepte)
                .reponsesRejetees(rejete)
                .reponsesEnAttente(attente)
                .progressPercent(BigDecimal.valueOf(progress)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue())
                .createdAt(c.getCreatedAt())
                .reponses(details)
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur courant introuvable"));
    }
}
