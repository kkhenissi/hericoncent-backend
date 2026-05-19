package com.hericonsent.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private String entiteType;
    private UUID entiteId;
    private UUID acteurId;
    private OffsetDateTime createdAt;
}
