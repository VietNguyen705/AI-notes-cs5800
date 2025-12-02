package com.notesapp.controllers;

import com.notesapp.entities.Note;
import com.notesapp.repositories.NoteRepository;
import com.notesapp.services.FileStorageService;
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

/**
 * REST controller for handling media uploads and downloads.
 */
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
     */
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("noteId") String noteId) {
        try {
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File must be an image"));
            }

            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                return ResponseEntity.badRequest().body(Map.of("error", "Image must be less than 10MB"));
            }

            String filename = fileStorageService.storeFile(file);
            String imageUrl = "/api/media/images/" + filename;

            Note note = noteRepository.findById(noteId).orElse(null);
            if (note != null) {
                note.getImages().add(imageUrl);
                noteRepository.save(note);
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("filename", filename);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }

    /**
     * Upload an audio recording and attach it to a note.
     */
    @PostMapping("/upload/audio")
    public ResponseEntity<Map<String, String>> uploadAudio(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("noteId") String noteId) {
        try {
            if (!file.getContentType().startsWith("audio/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File must be an audio file"));
            }

            if (file.getSize() > 25 * 1024 * 1024) { // 25MB limit
                return ResponseEntity.badRequest().body(Map.of("error", "Audio must be less than 25MB"));
            }

            String filename = fileStorageService.storeFile(file);
            String audioUrl = "/api/media/audio/" + filename;

            Note note = noteRepository.findById(noteId).orElse(null);
            if (note != null) {
                note.setVoiceRecording(audioUrl);
                noteRepository.save(note);
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", audioUrl);
            response.put("filename", filename);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload audio: " + e.getMessage()));
        }
    }

    /**
     * Get an uploaded image.
     */
    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            byte[] fileData = fileStorageService.loadFile(filename);
            String contentType = fileStorageService.getContentType(filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("public, max-age=31536000");

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get an uploaded audio file.
     */
    @GetMapping("/audio/{filename}")
    public ResponseEntity<byte[]> getAudio(@PathVariable String filename) {
        try {
            byte[] fileData = fileStorageService.loadFile(filename);
            String contentType = fileStorageService.getContentType(filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("public, max-age=31536000");

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete an image from a note.
     */
    @DeleteMapping("/images/{filename}")
    public ResponseEntity<Void> deleteImage(@PathVariable String filename,
                                            @RequestParam("noteId") String noteId) {
        try {
            Note note = noteRepository.findById(noteId).orElse(null);
            if (note != null) {
                String imageUrl = "/api/media/images/" + filename;
                note.getImages().remove(imageUrl);
                noteRepository.save(note);
            }

            fileStorageService.deleteFile(filename);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete audio from a note.
     */
    @DeleteMapping("/audio/{filename}")
    public ResponseEntity<Void> deleteAudio(@PathVariable String filename,
                                            @RequestParam("noteId") String noteId) {
        try {
            Note note = noteRepository.findById(noteId).orElse(null);
            if (note != null) {
                note.setVoiceRecording(null);
                noteRepository.save(note);
            }

            fileStorageService.deleteFile(filename);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
