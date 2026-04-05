package com.hericonsent.service;

import com.hericonsent.entity.AuditLog;
import com.hericonsent.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String action, String entiteType, UUID entiteId, UUID acteurId, Map<String, Object> payload) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entiteType(entiteType)
                .entiteId(entiteId)
                .acteurId(acteurId)
                .payload(payload)
                .build();
        auditLogRepository.save(log);
    }
}
