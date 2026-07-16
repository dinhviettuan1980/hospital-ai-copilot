package com.hospital.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Local-filesystem storage for uploaded files, shared by every module that
 * stores file bytes on disk with metadata in PostgreSQL (Knowledge Center,
 * Hospital Discovery attachments). Not a general-purpose object store —
 * just enough to keep the two callers from duplicating copy/delete logic.
 */
public class LocalFileStorage {

    private final Path root;

    public LocalFileStorage(String rootPath) {
        this.root = Path.of(rootPath);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create storage directory: " + root, e);
        }
    }

    /** Copies the source file into storage under a generated name, returning that name. */
    public String store(Path source, String originalFileName) {
        String storedFileName = UUID.randomUUID() + "." + extensionOf(originalFileName);
        try {
            Files.copy(source, root.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded file", e);
        }
        return storedFileName;
    }

    public Path resolve(String storedFileName) {
        return root.resolve(storedFileName);
    }

    public void deleteQuietly(String storedFileName) {
        try {
            Files.deleteIfExists(root.resolve(storedFileName));
        } catch (IOException e) {
            // Best-effort cleanup; the metadata row is already gone by the time this runs,
            // so an orphaned file on disk is not worth failing the request over.
        }
    }

    public static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
