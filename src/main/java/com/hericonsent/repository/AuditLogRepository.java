package com.hericonsent.repository;

import com.hericonsent.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntiteTypeAndEntiteIdOrderByCreatedAtDesc(String type, UUID id);

    List<AuditLog> findByActeurIdOrderByCreatedAtDesc(UUID acteurId);
}
