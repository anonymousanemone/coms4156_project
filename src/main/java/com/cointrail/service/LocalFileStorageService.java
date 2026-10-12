package com.cointrail.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Local file storage implementation.
 */
@Service
@Profile("local")
public final class LocalFileStorageService
    implements StorageService {

    /** The data directory path. */
    private final String dataDir =
        "src/main/resources/data/";

    /** {@inheritDoc} */
    @Override
    public String readFile(
        final String fileName
    ) throws IOException {
        return Files.readString(
            new File(dataDir + fileName).toPath()
        );
    }

    /** {@inheritDoc} */
    @Override
    public void writeFile(
        final String fileName,
        final String content
    ) throws IOException {
        Files.writeString(
            new File(dataDir + fileName).toPath(),
            content
        );
    }
}
