package com.hericonsent.repository;

import com.hericonsent.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

@Repository
interface PersonneRepository extends JpaRepository<Personne, UUID> {
    Optional<Personne> findByUserId(UUID userId);
}

@Repository
interface DossierRepository extends JpaRepository<Dossier, UUID> {
    List<Dossier> findByStatut(String statut);
    Optional<Dossier> findByReference(String reference);

    @Query("SELECT d FROM Dossier d JOIN d.heritiers h WHERE h.personne.user.id = :userId")
    List<Dossier> findByHeritierUserId(@Param("userId") UUID userId);

    @Query("SELECT d FROM Dossier d WHERE d.notaire.id = :notaireId")
    List<Dossier> findByNotaireId(@Param("notaireId") UUID notaireId);
}

@Repository
interface HeritierRepository extends JpaRepository<Heritier, UUID> {
    List<Heritier> findByDossierId(UUID dossierId);

    @Query("SELECT h FROM Heritier h WHERE h.dossier.id = :dossierId AND h.personne.user.id = :userId")
    Optional<Heritier> findByDossierIdAndUserId(
            @Param("dossierId") UUID dossierId,
            @Param("userId") UUID userId);
}

@Repository
interface ConsentementRepository extends JpaRepository<Consentement, UUID> {
    List<Consentement> findByDossierId(UUID dossierId);
    List<Consentement> findByDossierIdAndStatut(UUID dossierId, String statut);
}

@Repository
interface ConsentementReponseRepository extends JpaRepository<ConsentementReponse, UUID> {
    List<ConsentementReponse> findByConsentementId(UUID consentementId);
    Optional<ConsentementReponse> findByTokenAcces(String token);

    @Query("SELECT r FROM ConsentementReponse r WHERE r.consentement.id = :cid AND r.heritier.id = :hid")
    Optional<ConsentementReponse> findByConsentementIdAndHeritierID(
            @Param("cid") UUID cid, @Param("hid") UUID hid);

    @Query("SELECT COUNT(r) FROM ConsentementReponse r WHERE r.consentement.id = :cid AND r.reponse = :reponse")
    long countByConsentementIdAndReponse(@Param("cid") UUID cid, @Param("reponse") String reponse);
}

@Repository
interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByDossierId(UUID dossierId);
}

@Repository
interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntiteTypeAndEntiteIdOrderByCreatedAtDesc(String type, UUID id);
    List<AuditLog> findByActeurIdOrderByCreatedAtDesc(UUID acteurId);
}
