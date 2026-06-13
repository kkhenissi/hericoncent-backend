package com.hericonsent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.strategy", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Override
    public void store(String key, InputStream content, String contentType, long size) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build(),
            RequestBody.fromInputStream(content, size)
        );
        log.debug("Fichier uploadé sur S3 : s3://{}/{}", bucket, key);
    }

    @Override
    public Resource load(String key) {
        InputStream stream = s3Client.getObject(
            GetObjectRequest.builder().bucket(bucket).key(key).build()
        );
        return new InputStreamResource(stream);
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        );
        log.debug("Fichier supprimé de S3 : s3://{}/{}", bucket, key);
    }
}
