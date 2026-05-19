package com.hericonsent.repository;

import com.hericonsent.entity.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonneRepository extends JpaRepository<Personne, UUID> {
    Optional<Personne> findByUserId(UUID userId);
}
