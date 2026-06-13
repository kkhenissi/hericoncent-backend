package com.hericonsent.repository;

import com.hericonsent.entity.Consentement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentementRepository extends JpaRepository<Consentement, UUID> {
    List<Consentement> findByDossierId(UUID dossierId);

    List<Consentement> findByDossierIdAndStatut(UUID dossierId, String statut);

    boolean existsByDossierIdAndStatutIn(UUID dossierId, List<String> statuts);
}
