package com.hericonsent.service;

import com.hericonsent.dto.*;
import com.hericonsent.entity.*;
import com.hericonsent.exception.ResourceNotFoundException;
import com.hericonsent.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierService {

    private final DossierRepository dossierRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public DossierResponse creer(CreateDossierRequest request) {
        User currentUser = getCurrentUser();
        log.info("CreateDossierRequest reçue - notaireId: {}", request.getNotaireId());

        String reference = genererReference();

        Dossier dossier = Dossier.builder()
                .reference(reference)
                .titre(request.getTitre())
                .description(request.getDescription())
                .referenceCadastrale(request.getReferenceCadastrale())
                .adresseBien(request.getAdresseBien())
                .valeurEstimee(request.getValeurEstimee())
                .statut("OUVERT")
                .createdBy(currentUser)
                .build();

        if (request.getNotaireId() != null) {
            log.info("Assignation du notaire: {}", request.getNotaireId());
            User notaire = userRepository.findById(request.getNotaireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Notaire introuvable"));
            dossier.setNotaire(notaire);
            log.info("Notaire assigné au dossier: {}", notaire.getEmail());
        } else {
            log.info("Pas de notaireId fourni dans la requête");
        }

        dossier = dossierRepository.save(dossier);
        log.info("Dossier sauvegardé avec notaireId: {}", dossier.getNotaire() != null ? dossier.getNotaire().getId() : "NULL");

        auditService.log("CREATION_DOSSIER", "DOSSIER", dossier.getId(),
                currentUser.getId(), Map.of("reference", reference));

        log.info("Dossier créé : {} par {}", reference, currentUser.getEmail());
        return toResponse(dossier);
    }

    @Transactional(readOnly = true)
    public List<DossierResponse> listerTous(UUID notaireId) {
        User currentUser = getCurrentUser();
        List<Dossier> dossiers;

        if (currentUser.getRole().equals("ROLE_ADMIN")) {
            dossiers = notaireId != null
                    ? dossierRepository.findByNotaireId(notaireId)
                    : dossierRepository.findAll();
        } else if (currentUser.getRole().equals("ROLE_NOTAIRE")) {
            dossiers = dossierRepository.findByNotaireId(currentUser.getId());
        } else {
            dossiers = dossierRepository.findByHeritierUserId(currentUser.getId());
        }

        return dossiers.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DossierDetailResponse getDetail(UUID id) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + id));

        return DossierDetailResponse.builder()
                .id(dossier.getId())
                .reference(dossier.getReference())
                .titre(dossier.getTitre())
                .description(dossier.getDescription())
                .referenceCadastrale(dossier.getReferenceCadastrale())
                .adresseBien(dossier.getAdresseBien())
                .statut(dossier.getStatut())
                .valeurEstimee(dossier.getValeurEstimee())
                .createdAt(dossier.getCreatedAt())
                .heritiers(dossier.getHeritiers().stream()
                        .map(this::heritierToResponse)
                        .collect(Collectors.toList()))
                .consentements(dossier.getConsentements().stream()
                        .map(this::consentementToResponse)
                        .collect(Collectors.toList()))
                .documents(dossier.getDocuments().stream()
                        .map(this::documentToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public DossierResponse changerStatut(UUID id, String nouveauStatut) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + id));

        String ancienStatut = dossier.getStatut();
        dossier.setStatut(nouveauStatut);
        dossier = dossierRepository.save(dossier);

        User currentUser = getCurrentUser();
        auditService.log("CHANGEMENT_STATUT", "DOSSIER", dossier.getId(),
                currentUser.getId(),
                Map.of("ancien", ancienStatut, "nouveau", nouveauStatut));

        return toResponse(dossier);
    }

    // ---- Helpers ----

    private String genererReference() {
        int annee = java.time.LocalDate.now().getYear();
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "HC-" + annee + "-" + random;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur courant introuvable"));
    }

    private DossierResponse toResponse(Dossier d) {
        return DossierResponse.builder()
                .id(d.getId())
                .reference(d.getReference())
                .titre(d.getTitre())
                .description(d.getDescription())
                .referenceCadastrale(d.getReferenceCadastrale())
                .adresseBien(d.getAdresseBien())
                .statut(d.getStatut())
                .valeurEstimee(d.getValeurEstimee())
                .nombreHeritiers(d.getHeritiers().size())
                .nombreConsentements(d.getConsentements().size())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private HeritierResponse heritierToResponse(Heritier h) {
        return HeritierResponse.builder()
                .id(h.getId())
                .personneId(h.getPersonne().getId())
                .nomComplet(h.getPersonne().getNomComplet())
                .email(h.getPersonne().getEmail())
                .telephone(h.getPersonne().getTelephone())
                .part(h.getPart())
                .role(h.getRole())
                .statutContact(h.getStatutContact())
                .identityVerified(h.getPersonne().isIdentityVerified())
                .build();
    }

    private ConsentementResponse consentementToResponse(Consentement c) {
        return ConsentementResponse.builder()
                .id(c.getId())
                .titre(c.getTitre())
                .typeAction(c.getTypeAction())
                .statut(c.getStatut())
                .seuilAccord(c.getSeuilAccord())
                .expireLe(c.getExpireLe())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private DocumentResponse documentToResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .nom(doc.getNom())
                .typeDoc(doc.getTypeDoc())
                .mimeType(doc.getMimeType())
                .taille(doc.getTaille())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
