package com.notesapp.controllers;

import com.notesapp.repositories.NoteRepository;
import com.notesapp.services.PDFExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * REST controller for exporting notes to various formats.
 */
@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private PDFExportService pdfExportService;

    @Autowired
    private NoteRepository noteRepository;

    /**
     * Export a single note to PDF.
     */
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportNoteToPDF(@PathVariable String id,
                                                   @RequestParam(defaultValue = "true") boolean includeMetadata) {
        try {
            byte[] pdfBytes = pdfExportService.exportNoteToPDF(id, includeMetadata);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "note-" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export multiple notes to a single PDF.
     */
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportMultipleNotesToPDF(@RequestBody Map<String, Object> exportData) {
        try {
            @SuppressWarnings("unchecked")
            List<String> noteIds = (List<String>) exportData.get("noteIds");
            boolean includeMetadata = (boolean) exportData.getOrDefault("includeMetadata", true);

            if (noteIds == null || noteIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] pdfBytes = pdfExportService.exportMultipleNotesToPDF(noteIds, includeMetadata);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "notes-export-" + timestamp + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export all notes for a user to PDF.
     */
    @GetMapping("/export/all/pdf")
    public ResponseEntity<byte[]> exportAllNotesToPDF(@RequestParam String userId,
                                                       @RequestParam(defaultValue = "true") boolean includeMetadata) {
        try {
            List<String> noteIds = noteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(note -> note.getNoteId())
                .toList();

            if (noteIds.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            byte[] pdfBytes = pdfExportService.exportMultipleNotesToPDF(noteIds, includeMetadata);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "all-notes-" + timestamp + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
