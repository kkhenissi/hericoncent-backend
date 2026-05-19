package com.hericonsent.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class DocumentResponse {
    private UUID id;
    private String nom;
    private String typeDoc;
    private String mimeType;
    private Long taille;
    private OffsetDateTime uploadedAt;
    private String downloadUrl;
}
