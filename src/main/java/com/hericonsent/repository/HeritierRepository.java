package com.hericonsent.repository;

import com.hericonsent.entity.Heritier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HeritierRepository extends JpaRepository<Heritier, UUID> {
    List<Heritier> findByDossierId(UUID dossierId);

    @Query("SELECT h FROM Heritier h WHERE h.dossier.id = :dossierId AND h.personne.user.id = :userId")
    Optional<Heritier> findByDossierIdAndUserId(
            @Param("dossierId") UUID dossierId,
            @Param("userId") UUID userId);
}
