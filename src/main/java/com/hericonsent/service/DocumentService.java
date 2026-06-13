package com.hericonsent.service;

import com.hericonsent.dto.DocumentResponse;
import com.hericonsent.entity.Document;
import com.hericonsent.entity.Dossier;
import com.hericonsent.entity.User;
import com.hericonsent.exception.ResourceNotFoundException;
import com.hericonsent.repository.DocumentRepository;
import com.hericonsent.repository.DossierRepository;
import com.hericonsent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DossierRepository dossierRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final StorageService storageService;

    // ============================================
    // UPLOAD
    // ============================================
    @Transactional
    public DocumentResponse upload(UUID dossierId, MultipartFile file, String typeDoc) throws IOException {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + dossierId));

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String key = dossierId + "/" + UUID.randomUUID() + ext;

        byte[] bytes = file.getBytes();
        String checksum = DigestUtils.md5DigestAsHex(bytes);
        storageService.store(key, new ByteArrayInputStream(bytes), file.getContentType(), file.getSize());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User uploadeur = userRepository.findByEmail(email).orElse(null);

        Document document = Document.builder()
                .dossier(dossier)
                .nom(originalName)
                .typeDoc(typeDoc != null ? typeDoc : "AUTRE")
                .s3Key(key)
                .mimeType(file.getContentType())
                .taille(file.getSize())
                .checksum(checksum)
                .uploadPar(uploadeur)
                .build();

        document = documentRepository.save(document);

        auditService.log("UPLOAD_DOCUMENT", "DOCUMENT", document.getId(),
                uploadeur != null ? uploadeur.getId() : null,
                Map.of("dossier", dossierId.toString(), "nom", originalName));

        log.info("Document '{}' uploadé pour dossier {}", originalName, dossierId);
        return toResponse(document);
    }

    // ============================================
    // DOWNLOAD
    // ============================================
    @Transactional(readOnly = true)
    public Resource download(UUID documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : " + documentId));
        return storageService.load(document.getS3Key());
    }

    @Transactional(readOnly = true)
    public Document getById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : " + documentId));
    }

    // ============================================
    // SUPPRESSION
    // ============================================
    @Transactional
    public void supprimer(UUID documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : " + documentId));

        storageService.delete(document.getS3Key());
        documentRepository.delete(document);

        auditService.log("SUPPRESSION_DOCUMENT", "DOCUMENT", documentId, null, null);
        log.info("Document {} supprimé", documentId);
    }

    // ============================================
    // MAPPER
    // ============================================
    public DocumentResponse toResponse(Document d) {
        return DocumentResponse.builder()
                .id(d.getId())
                .nom(d.getNom())
                .typeDoc(d.getTypeDoc())
                .mimeType(d.getMimeType())
                .taille(d.getTaille())
                .uploadedAt(d.getUploadedAt())
                .downloadUrl("/api/documents/" + d.getId() + "/download")
                .build();
    }
}
