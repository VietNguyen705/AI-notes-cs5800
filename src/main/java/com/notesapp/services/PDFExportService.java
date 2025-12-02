package com.notesapp.services;

import com.notesapp.entities.ChecklistItem;
import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.entities.TodoItem;
import com.notesapp.repositories.NoteRepository;
import com.notesapp.repositories.TaskRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for exporting notes to PDF format.
 */
@Service
public class PDFExportService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TaskRepository taskRepository;

    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADING = 14;
    private static final float FONT_SIZE_BODY = 12;
    private static final float FONT_SIZE_SMALL = 10;
    private static final float LINE_HEIGHT = 15;

    /**
     * Export a single note to PDF.
     */
    public byte[] exportNoteToPDF(String noteId, boolean includeMetadata) throws IOException {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found: " + noteId));

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                yPosition = addTitle(contentStream, note.getTitle(), yPosition);
                yPosition -= LINE_HEIGHT;

                if (includeMetadata) {
                    yPosition = addMetadata(contentStream, note, yPosition);
                    yPosition -= LINE_HEIGHT;
                }

                yPosition = addNoteBody(contentStream, note.getBody(), yPosition, page);

                if (note.getTags() != null && !note.getTags().isEmpty()) {
                    yPosition -= LINE_HEIGHT;
                    yPosition = addTags(contentStream, new ArrayList<>(note.getTags()), yPosition);
                }

                List<TodoItem> todos = taskRepository.findByNoteId(noteId);
                if (todos != null && !todos.isEmpty()) {
                    yPosition -= LINE_HEIGHT;
                    yPosition = addTodoItems(contentStream, todos, yPosition);
                }

                if (note.getChecklist() != null && !note.getChecklist().isEmpty()) {
                    yPosition -= LINE_HEIGHT;
                    yPosition = addChecklistItems(contentStream, note.getChecklist(), yPosition);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Export multiple notes to a single PDF.
     */
    public byte[] exportMultipleNotesToPDF(List<String> noteIds, boolean includeMetadata) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (String noteId : noteIds) {
                Note note = noteRepository.findById(noteId).orElse(null);
                if (note == null) {
                    continue;
                }

                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    float yPosition = page.getMediaBox().getHeight() - MARGIN;

                    yPosition = addTitle(contentStream, note.getTitle(), yPosition);
                    yPosition -= LINE_HEIGHT;

                    if (includeMetadata) {
                        yPosition = addMetadata(contentStream, note, yPosition);
                        yPosition -= LINE_HEIGHT;
                    }

                    yPosition = addNoteBody(contentStream, note.getBody(), yPosition, page);

                    if (note.getTags() != null && !note.getTags().isEmpty()) {
                        yPosition -= LINE_HEIGHT;
                        yPosition = addTags(contentStream, new ArrayList<>(note.getTags()), yPosition);
                    }

                    List<TodoItem> todos = taskRepository.findByNoteId(note.getNoteId());
                    if (todos != null && !todos.isEmpty()) {
                        yPosition -= LINE_HEIGHT;
                        yPosition = addTodoItems(contentStream, todos, yPosition);
                    }

                    if (note.getChecklist() != null && !note.getChecklist().isEmpty()) {
                        yPosition -= LINE_HEIGHT;
                        yPosition = addChecklistItems(contentStream, note.getChecklist(), yPosition);
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private float addTitle(PDPageContentStream contentStream, String title, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title != null ? title : "Untitled");
        contentStream.endText();
        return yPosition - FONT_SIZE_TITLE - 5;
    }

    private float addMetadata(PDPageContentStream contentStream, Note note, float yPosition) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String createdAt = note.getCreatedAt() != null ? note.getCreatedAt().format(formatter) : "";
        String category = note.getCategory() != null ? note.getCategory() : "";

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Created: " + createdAt);
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        if (!category.isEmpty()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Category: " + category);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }

        return yPosition;
    }

    private float addNoteBody(PDPageContentStream contentStream, String body, float yPosition, PDPage page) throws IOException {
        if (body == null || body.isEmpty()) {
            return yPosition;
        }

        String cleanBody = stripHtmlTags(body);
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY);

        float pageWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
        List<String> lines = wrapText(cleanBody, pageWidth, PDType1Font.HELVETICA, FONT_SIZE_BODY);

        for (String line : lines) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(line);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;

            if (yPosition < MARGIN) {
                break;
            }
        }

        return yPosition;
    }

    private float addTags(PDPageContentStream contentStream, List<Tag> tags, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_HEADING);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Tags:");
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY);
        String tagNames = tags.stream()
            .map(Tag::getName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 10, yPosition);
        contentStream.showText(tagNames);
        contentStream.endText();

        return yPosition - LINE_HEIGHT;
    }

    private float addTodoItems(PDPageContentStream contentStream, List<TodoItem> todos, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_HEADING);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("To-Do Items:");
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY);
        for (TodoItem todo : todos) {
            String status = todo.getStatus() != null ? "[" + todo.getStatus() + "] " : "";
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 10, yPosition);
            contentStream.showText("• " + status + todo.getTitle());
            contentStream.endText();
            yPosition -= LINE_HEIGHT;

            if (yPosition < MARGIN) {
                break;
            }
        }

        return yPosition;
    }

    private float addChecklistItems(PDPageContentStream contentStream, List<ChecklistItem> items, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_HEADING);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Checklist:");
        contentStream.endText();
        yPosition -= LINE_HEIGHT;

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY);
        for (ChecklistItem item : items) {
            String checkbox = item.getIsChecked() ? "[X] " : "[ ] ";
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 10, yPosition);
            contentStream.showText(checkbox + item.getText());
            contentStream.endText();
            yPosition -= LINE_HEIGHT;

            if (yPosition < MARGIN) {
                break;
            }
        }

        return yPosition;
    }

    private String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
    }

    private List<String> wrapText(String text, float width, PDType1Font font, float fontSize) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (textWidth > width) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
