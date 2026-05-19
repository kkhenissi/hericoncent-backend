package com.hericonsent.repository;

import com.hericonsent.entity.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, UUID> {
    List<Dossier> findByStatut(String statut);

    Optional<Dossier> findByReference(String reference);

    @Query("SELECT d FROM Dossier d JOIN d.heritiers h WHERE h.personne.user.id = :userId")
    List<Dossier> findByHeritierUserId(@Param("userId") UUID userId);

    @Query("SELECT d FROM Dossier d WHERE d.notaire.id = :notaireId")
    List<Dossier> findByNotaireId(@Param("notaireId") UUID notaireId);
}
