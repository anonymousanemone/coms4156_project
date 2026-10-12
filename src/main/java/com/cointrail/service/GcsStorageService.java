package com.cointrail.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Google Cloud Storage implementation.
 */
@Service
@Profile("!test & !local")
public final class GcsStorageService
    implements StorageService {

    /** The GCS storage client. */
    private final Storage storage =
        StorageOptions.getDefaultInstance()
            .getService();

    /** The GCS bucket name. */
    @Value("${gcs.bucket-name}")
    private String bucketName;

    /**
     * Initializes seed data in GCS if missing.
     *
     * @throws IOException if an I/O error occurs
     */
    @PostConstruct
    public void initializeData() throws IOException {
        String[] files = {
            "coins.json",
            "museums.json",
            "donors.json",
            "acquisitions.json",
        };
        for (String fileName : files) {
            BlobId blobId =
                BlobId.of(bucketName, fileName);
            if (storage.get(blobId) == null) {
                byte[] content;
                try (InputStream is =
                    getClass().getResourceAsStream(
                        "/data/" + fileName
                    )) {
                        content = is.readAllBytes();
                    }
                BlobInfo blobInfo = BlobInfo
                    .newBuilder(blobId)
                    .setContentType(
                        "application/json"
                    )
                    .build();
                storage.create(blobInfo, content);
            }
        }
    }

    /** {@inheritDoc} */
    public String readFile(
        final String fileName
    ) {
        BlobId blobId =
            BlobId.of(bucketName, fileName);
        byte[] content =
            storage.readAllBytes(blobId);
        return new String(
            content, StandardCharsets.UTF_8
        );
    }

    /** {@inheritDoc} */
    public void writeFile(
        final String fileName,
        final String content
    ) {
        BlobId blobId =
            BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo
            .newBuilder(blobId)
            .setContentType("application/json")
            .build();
        storage.create(
            blobInfo,
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
