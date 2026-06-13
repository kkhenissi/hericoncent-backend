package com.hericonsent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.strategy", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void store(String key, InputStream content, String contentType, long size) throws IOException {
        Path dest = Paths.get(uploadDir).resolve(key).normalize();
        Files.createDirectories(dest.getParent());
        Files.copy(content, dest, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Fichier stocké localement : {}", dest);
    }

    @Override
    public Resource load(String key) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(key).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Fichier introuvable sur le serveur : " + key);
        }
        return resource;
    }

    @Override
    public void delete(String key) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(key).normalize();
        Files.deleteIfExists(filePath);
        log.debug("Fichier supprimé : {}", filePath);
    }
}
