package com.hericonsent.service;

import com.hericonsent.dto.AddHeritierRequest;
import com.hericonsent.dto.HeritierResponse;
import com.hericonsent.dto.UpdateHeritierRequest;
import com.hericonsent.entity.Dossier;
import com.hericonsent.entity.Heritier;
import com.hericonsent.entity.Personne;
import com.hericonsent.exception.ResourceNotFoundException;
import com.hericonsent.repository.DossierRepository;
import com.hericonsent.repository.HeritierRepository;
import com.hericonsent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeritierService {

    private final HeritierRepository heritierRepository;
    private final DossierRepository dossierRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public HeritierResponse ajouter(UUID dossierId, AddHeritierRequest request) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + dossierId));

        if (request.getPart() != null && request.getPart().compareTo(BigDecimal.ZERO) > 0) {
            validerSommeParts(dossierId, null, request.getPart());
        }

        Personne personne = Personne.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .dateNaissance(request.getDateNaissance())
                .adresse(request.getAdresse())
                .gender(request.getGender())
                .build();

        Heritier heritier = Heritier.builder()
                .dossier(dossier)
                .personne(personne)
                .part(request.getPart())
                .role(request.getRole())
                .validated(request.isValidated())
                .isHeir(request.isHeir())
                .statutContact("NON_CONTACTE")
                .build();

        heritier = heritierRepository.save(heritier);

        String acteurEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        auditService.log("AJOUT_HERITIER", "HERITIER", heritier.getId(), null,
                Map.of("dossier", dossierId.toString(),
                        "heritier", personne.getNomComplet()));

        log.info("Héritier {} ajouté au dossier {}", personne.getNomComplet(), dossierId);
        return toResponse(heritier);
    }

    @Transactional(readOnly = true)
    public List<HeritierResponse> listerParDossier(UUID dossierId) {
        return heritierRepository.findByDossierId(dossierId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HeritierResponse mettreAJour(UUID heritierId, UpdateHeritierRequest request) {
        Heritier h = heritierRepository.findById(heritierId)
                .orElseThrow(() -> new ResourceNotFoundException("Héritier introuvable"));

        if (request.getEmail() != null) {
            h.getPersonne().setEmail(request.getEmail());
        }
        if (request.getPart() != null) {
            validerSommeParts(h.getDossier().getId(), h.getId(), request.getPart());
            h.setPart(request.getPart());
        }

        h = heritierRepository.save(h);
        auditService.log("MAJ_HERITIER", "HERITIER", heritierId, null,
                Map.of("heritier", h.getPersonne().getNomComplet()));
        return toResponse(h);
    }

    @Transactional
    public void supprimer(UUID heritierId) {
        Heritier h = heritierRepository.findById(heritierId)
                .orElseThrow(() -> new ResourceNotFoundException("Héritier introuvable"));
        heritierRepository.delete(h);
        auditService.log("SUPPRESSION_HERITIER", "HERITIER", heritierId, null, null);
    }

    @Transactional
    public HeritierResponse mettreAJourStatutContact(UUID heritierId, String statut) {
        Heritier h = heritierRepository.findById(heritierId)
                .orElseThrow(() -> new ResourceNotFoundException("Héritier introuvable"));
        h.setStatutContact(statut);
        h = heritierRepository.save(h);
        return toResponse(h);
    }

    /**
     * Vérifie que la somme des parts du dossier ne dépasse pas 1.0 après l'ajout/modification.
     * @param dossierId  le dossier concerné
     * @param excludeId  l'UUID de l'héritier à exclure du calcul (null lors d'un ajout)
     * @param nouvellePart la part à attribuer
     */
    private void validerSommeParts(UUID dossierId, UUID excludeId, BigDecimal nouvellePart) {
        BigDecimal sommeExistante = heritierRepository.findByDossierId(dossierId).stream()
                .filter(h -> excludeId == null || !h.getId().equals(excludeId))
                .map(Heritier::getPart)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = sommeExistante.add(nouvellePart);
        if (total.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal disponible = BigDecimal.ONE.subtract(sommeExistante);
            throw new IllegalArgumentException(
                    "La somme des parts dépasserait 100 %. Part disponible restante : "
                    + disponible.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + " %");
        }
    }

    private HeritierResponse toResponse(Heritier h) {
        return HeritierResponse.builder()
                .id(h.getId())
                .personneId(h.getPersonne().getId())
                .nomComplet(h.getPersonne().getNomComplet())
                .email(h.getPersonne().getEmail())
                .telephone(h.getPersonne().getTelephone())
                .part(h.getPart())
                .role(h.getRole())
                .validated(h.isValidated())
                .isHeir(h.isHeir())
                .statutContact(h.getStatutContact())
                .identityVerified(h.getPersonne().isIdentityVerified())
                .build();
    }
}
