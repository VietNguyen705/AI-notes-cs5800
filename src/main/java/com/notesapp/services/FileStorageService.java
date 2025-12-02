package com.notesapp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Service for storing and retrieving uploaded media files.
 * Implements security measures to prevent path traversal attacks.
 */
@Slf4j
@Service
public class FileStorageService {

  private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
      "jpg", "image/jpeg",
      "jpeg", "image/jpeg",
      "png", "image/png",
      "gif", "image/gif",
      "webp", "image/webp",
      "mp3", "audio/mpeg",
      "wav", "audio/wav",
      "ogg", "audio/ogg",
      "mp4", "video/mp4",
      "webm", "video/webm"
  );

  private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

  private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

  public FileStorageService() {
    try {
      Files.createDirectories(uploadDir);
      log.info("Upload directory initialized: {}", uploadDir);
    } catch (IOException e) {
      log.error("Failed to create upload directory", e);
      throw new RuntimeException("Could not create upload directory", e);
    }
  }

  /**
   * Stores an uploaded file with a unique filename.
   *
   * @param file the file to store
   * @return the generated filename
   * @throws IOException if file storage fails
   * @throws IllegalArgumentException if file is empty
   */
  public String storeFile(MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Cannot store empty file");
    }

    String originalFilename = file.getOriginalFilename();
    String extension = extractFileExtension(originalFilename);
    String filename = generateUniqueFilename(extension);

    Path targetLocation = uploadDir.resolve(filename);
    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

    log.debug("Stored file: {}", filename);
    return filename;
  }

  /**
   * Loads a file's content by filename.
   * Validates filename to prevent path traversal attacks.
   *
   * @param filename the filename to load
   * @return the file content as byte array
   * @throws IOException if file is not found or cannot be read
   * @throws SecurityException if filename is invalid or attempts path traversal
   */
  public byte[] loadFile(String filename) throws IOException {
    validateFilename(filename);

    Path filePath = uploadDir.resolve(filename).normalize();

    if (!filePath.startsWith(uploadDir)) {
      log.warn("Path traversal attempt detected: {}", filename);
      throw new SecurityException("Access denied: invalid file path");
    }

    if (!Files.exists(filePath)) {
      throw new IOException("File not found: " + filename);
    }

    return Files.readAllBytes(filePath);
  }

  /**
   * Deletes a file by filename.
   * Validates filename to prevent path traversal attacks.
   *
   * @param filename the filename to delete
   * @throws IOException if deletion fails
   * @throws SecurityException if filename is invalid
   */
  public void deleteFile(String filename) throws IOException {
    validateFilename(filename);

    Path filePath = uploadDir.resolve(filename).normalize();

    if (!filePath.startsWith(uploadDir)) {
      log.warn("Path traversal attempt detected during deletion: {}", filename);
      throw new SecurityException("Access denied: invalid file path");
    }

    Files.deleteIfExists(filePath);
    log.debug("Deleted file: {}", filename);
  }

  /**
   * Gets the content type for a filename based on extension.
   *
   * @param filename the filename
   * @return the MIME content type
   */
  public String getContentType(String filename) {
    String extension = extractFileExtension(filename);
    return CONTENT_TYPE_MAP.getOrDefault(extension.toLowerCase(), DEFAULT_CONTENT_TYPE);
  }

  /**
   * Validates filename to prevent security issues.
   *
   * @param filename the filename to validate
   * @throws SecurityException if filename is invalid
   */
  private void validateFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      throw new SecurityException("Filename cannot be null or empty");
    }

    if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
      throw new SecurityException("Invalid filename: contains path traversal characters");
    }

    if (filename.length() > 255) {
      throw new SecurityException("Filename too long");
    }
  }

  /**
   * Extracts file extension from filename.
   *
   * @param filename the filename
   * @return the file extension (without dot), or empty string if none
   */
  private String extractFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf(".") + 1);
  }

  /**
   * Generates a unique filename with the given extension.
   *
   * @param extension the file extension (without dot)
   * @return a unique filename
   */
  private String generateUniqueFilename(String extension) {
    String uuid = UUID.randomUUID().toString();
    return extension.isEmpty() ? uuid : uuid + "." + extension;
  }
}
