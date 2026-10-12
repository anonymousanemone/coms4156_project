package com.cointrail.service;

import java.io.IOException;

/**
 * Interface for storage operations.
 */
public interface StorageService {

    /**
     * Reads a file by name.
     *
     * @param fileName the file name
     * @return the file content
     * @throws IOException if an I/O error occurs
     */
    String readFile(String fileName) throws IOException;

    /**
     * Writes content to a file.
     *
     * @param fileName the file name
     * @param content the content to write
     * @throws IOException if an I/O error occurs
     */
    void writeFile(String fileName, String content)
        throws IOException;
}
