package com.hericonsent.repository;

import com.hericonsent.entity.ConsentementReponse;
import com.hericonsent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentementReponseRepository extends JpaRepository<ConsentementReponse, UUID> {
    List<ConsentementReponse> findByConsentementId(UUID consentementId);

    Optional<ConsentementReponse> findByTokenAcces(String token);

    @Query("SELECT r FROM ConsentementReponse r WHERE r.consentement.id = :cid AND r.heritier.id = :hid")
    Optional<ConsentementReponse> findByConsentementIdAndHeritierID(
            @Param("cid") UUID cid, @Param("hid") UUID hid);

    @Query("SELECT COUNT(r) FROM ConsentementReponse r WHERE r.consentement.id = :cid AND r.reponse = :reponse")
    long countByConsentementIdAndReponse(@Param("cid") UUID cid, @Param("reponse") String reponse);

    @Repository
    interface UserRepository extends JpaRepository<User, UUID> {
        Optional<User> findByEmail(String email);
        boolean existsByEmail(String email);
    }
}
