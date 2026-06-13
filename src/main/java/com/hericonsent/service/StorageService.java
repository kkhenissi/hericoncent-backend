package com.hericonsent.service;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstraction du stockage de fichiers — local ou S3/MinIO.
 * La clé (key) correspond au champ {@code Document.s3Key} : "{dossierId}/{uuid}.{ext}".
 */
public interface StorageService {

    /**
     * Persiste le contenu sous la clé donnée.
     *
     * @param key         chemin relatif ou clé S3, ex. "550e8400-.../doc.pdf"
     * @param content     flux du fichier (consommé une seule fois)
     * @param contentType MIME type, ex. "application/pdf"
     * @param size        taille en octets (requise pour S3)
     */
    void store(String key, InputStream content, String contentType, long size) throws IOException;

    /**
     * Charge le fichier identifié par la clé.
     *
     * @throws IOException               si la lecture échoue
     * @throws IllegalStateException     si la ressource est absente ou illisible
     */
    Resource load(String key) throws IOException;

    /** Supprime le fichier identifié par la clé. Sans effet si absent. */
    void delete(String key) throws IOException;
}
