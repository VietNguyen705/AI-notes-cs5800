package com.notesapp.controllers;

import com.notesapp.config.AppConstants;
import com.notesapp.entities.Note;
import com.notesapp.repositories.NoteRepository;
import com.notesapp.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * REST controller for handling media uploads and downloads.
 * Supports image and audio file management with note attachments.
 */
@Slf4j
@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*")
public class MediaController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private NoteRepository noteRepository;

    /**
     * Upload an image and attach it to a note.
     *
     * @param file   the image file to upload (must be image/* content type, max 10MB)
     * @param noteId the ID of the note to attach the image to
     * @return response containing the image URL and filename, or error message
     */
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("noteId") String noteId) {
        log.info("Uploading image for note: {}", noteId);
        return uploadFile(
                file,
                noteId,
                "image/",
                AppConstants.MAX_IMAGE_SIZE_BYTES,
                (note, url) -> note.getImages().add(url),
                "image"
        );
    }

    /**
     * Upload an audio recording and attach it to a note.
     *
     * @param file   the audio file to upload (must be audio/* content type, max 25MB)
     * @param noteId the ID of the note to attach the audio to
     * @return response containing the audio URL and filename, or error message
     */
    @PostMapping("/upload/audio")
    public ResponseEntity<Map<String, String>> uploadAudio(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("noteId") String noteId) {
        log.info("Uploading audio for note: {}", noteId);
        return uploadFile(
                file,
                noteId,
                "audio/",
                AppConstants.MAX_AUDIO_SIZE_BYTES,
                (note, url) -> note.setVoiceRecording(url),
                "audio"
        );
    }

    /**
     * Get an uploaded image by filename.
     *
     * @param filename the name of the image file
     * @return the image file data with appropriate content type, or 404 if not found
     */
    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        log.debug("Retrieving image: {}", filename);
        return getFile(filename);
    }

    /**
     * Get an uploaded audio file by filename.
     *
     * @param filename the name of the audio file
     * @return the audio file data with appropriate content type, or 404 if not found
     */
    @GetMapping("/audio/{filename}")
    public ResponseEntity<byte[]> getAudio(@PathVariable String filename) {
        log.debug("Retrieving audio: {}", filename);
        return getFile(filename);
    }

    /**
     * Delete an image from a note and from storage.
     *
     * @param filename the name of the image file to delete
     * @param noteId   the ID of the note containing the image
     * @return 200 OK if successful, or 500 if an error occurs
     */
    @DeleteMapping("/images/{filename}")
    public ResponseEntity<Void> deleteImage(@PathVariable String filename,
                                            @RequestParam("noteId") String noteId) {
        log.info("Deleting image {} from note: {}", filename, noteId);
        return deleteFileFromNote(
                filename,
                noteId,
                (note, url) -> note.getImages().remove(url),
                "images"
        );
    }

    /**
     * Delete audio from a note and from storage.
     *
     * @param filename the name of the audio file to delete
     * @param noteId   the ID of the note containing the audio
     * @return 200 OK if successful, or 500 if an error occurs
     */
    @DeleteMapping("/audio/{filename}")
    public ResponseEntity<Void> deleteAudio(@PathVariable String filename,
                                            @RequestParam("noteId") String noteId) {
        log.info("Deleting audio {} from note: {}", filename, noteId);
        return deleteFileFromNote(
                filename,
                noteId,
                (note, url) -> note.setVoiceRecording(null),
                "audio"
        );
    }

    /**
     * Generic file upload handler for images and audio files.
     *
     * @param file              the file to upload
     * @param noteId            the ID of the note to attach the file to
     * @param contentTypePrefix the expected content type prefix (e.g., "image/", "audio/")
     * @param maxSize           maximum allowed file size in bytes
     * @param noteUpdater       function to update the note with the file URL
     * @param mediaType         the type of media for URL construction ("image" or "audio")
     * @return response containing the file URL and filename, or error message
     */
    private ResponseEntity<Map<String, String>> uploadFile(MultipartFile file,
                                                            String noteId,
                                                            String contentTypePrefix,
                                                            long maxSize,
                                                            BiConsumer<Note, String> noteUpdater,
                                                            String mediaType) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith(contentTypePrefix)) {
                String errorMsg = String.format("File must be a %s file (received: %s)",
                        contentTypePrefix.replace("/", ""), contentType);
                log.warn("Invalid file type for {}: {}", mediaType, contentType);
                return ResponseEntity.badRequest().body(buildErrorResponse(errorMsg));
            }

            if (file.getSize() > maxSize) {
                String errorMsg = String.format("%s must be less than %dMB (received: %dMB)",
                        capitalize(mediaType),
                        maxSize / (1024 * 1024),
                        file.getSize() / (1024 * 1024));
                log.warn("File too large for {}: {} bytes (max: {} bytes)", mediaType, file.getSize(), maxSize);
                return ResponseEntity.badRequest().body(buildErrorResponse(errorMsg));
            }

            String filename = fileStorageService.storeFile(file);
            String fileUrl = String.format("/api/media/%s/%s", mediaType, filename);

            Note note = noteRepository.findById(noteId).orElse(null);
            if (note != null) {
                noteUpdater.accept(note, fileUrl);
                noteRepository.save(note);
                log.info("Successfully uploaded {} {} and attached to note {}", mediaType, filename, noteId);
            } else {
                log.warn("Note not found: {}. File uploaded but not attached.", noteId);
            }

            return ResponseEntity.ok(buildSuccessResponse(fileUrl, filename));
        } catch (IOException e) {
            String errorMsg = String.format("Failed to upload %s: %s", mediaType, e.getMessage());
            log.error("Error uploading {}: {}", mediaType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse(errorMsg));
        }
    }

    /**
     * Generic file retrieval handler for images and audio files.
     *
     * @param filename the name of the file to retrieve
     * @return the file data with appropriate content type, or 404 if not found
     */
    private ResponseEntity<byte[]> getFile(String filename) {
        try {
            byte[] fileData = fileStorageService.loadFile(filename);
            String contentType = fileStorageService.getContentType(filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("public, max-age=31536000");

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("File not found: {}", filename);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Generic file deletion handler for images and audio files.
     *
     * @param filename     the name of the file to delete
     * @param noteId       the ID of the note containing the file
     * @param noteUpdater  function to update the note to remove the file reference
     * @param mediaType    the type of media being deleted ("images" or "audio")
     * @return 200 OK if successful, or 500 if an error occurs
     */
    private ResponseEntity<Void> deleteFileFromNote(String filename,
                                                     String noteId,
                                                     BiConsumer<Note, String> noteUpdater,
                                                     String mediaType) {
        try {
            Note note = noteRepository.findById(noteId).orElse(null);
            if (note != null) {
                String fileUrl = String.format("/api/media/%s/%s", mediaType, filename);
                noteUpdater.accept(note, fileUrl);
                noteRepository.save(note);
                log.info("Removed {} reference from note {}", mediaType, noteId);
            } else {
                log.warn("Note not found: {}. Proceeding with file deletion anyway.", noteId);
            }

            fileStorageService.deleteFile(filename);
            log.info("Successfully deleted {} file: {}", mediaType, filename);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Error deleting {} file {}: {}", mediaType, filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Builds an error response map.
     *
     * @param message the error message
     * @return a map containing the error message
     */
    private Map<String, String> buildErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    /**
     * Builds a success response map with file URL and filename.
     *
     * @param url      the URL where the file can be accessed
     * @param filename the name of the uploaded file
     * @return a map containing the URL and filename
     */
    private Map<String, String> buildSuccessResponse(String url, String filename) {
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        response.put("filename", filename);
        return response;
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
