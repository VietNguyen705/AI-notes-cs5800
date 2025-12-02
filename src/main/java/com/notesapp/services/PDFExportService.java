package com.notesapp.services;

import com.notesapp.config.AppConstants;
import com.notesapp.entities.ChecklistItem;
import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.entities.TodoItem;
import com.notesapp.repositories.NoteRepository;
import com.notesapp.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Service for exporting notes to PDF format.
 */
@Slf4j
@Service
public class PDFExportService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TaskRepository taskRepository;

    private static final float FONT_SIZE_SMALL = 10f;
    private static final float TITLE_VERTICAL_SPACING = 5f;
    private static final float CONTENT_INDENT = 10f;

    /**
     * Export a single note to PDF.
     *
     * @param noteId The ID of the note to export
     * @param includeMetadata Whether to include metadata (created date, category) in the PDF
     * @return PDF file as byte array
     * @throws IOException If PDF generation fails
     * @throws IllegalArgumentException If note is not found
     */
    public byte[] exportNoteToPDF(String noteId, boolean includeMetadata) throws IOException {
        log.info("Starting PDF export for note: {}", noteId);

        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found: " + noteId));

        try (PDDocument document = new PDDocument()) {
            addNoteToDocument(document, note, includeMetadata);
            byte[] pdfBytes = convertDocumentToBytes(document);

            log.info("Successfully exported note {} to PDF ({} bytes)", noteId, pdfBytes.length);
            return pdfBytes;
        }
    }

    /**
     * Export multiple notes to a single PDF document.
     *
     * @param noteIds List of note IDs to export
     * @param includeMetadata Whether to include metadata in the PDF
     * @return PDF file as byte array containing all notes
     * @throws IOException If PDF generation fails
     */
    public byte[] exportMultipleNotesToPDF(List<String> noteIds, boolean includeMetadata) throws IOException {
        log.info("Starting PDF export for {} notes", noteIds.size());

        try (PDDocument document = new PDDocument()) {
            int exportedCount = 0;

            for (String noteId : noteIds) {
                Note note = noteRepository.findById(noteId).orElse(null);
                if (note != null) {
                    addNoteToDocument(document, note, includeMetadata);
                    exportedCount++;
                }
            }

            byte[] pdfBytes = convertDocumentToBytes(document);
            log.info("Successfully exported {} notes to PDF ({} bytes)", exportedCount, pdfBytes.length);
            return pdfBytes;
        }
    }

    /**
     * Adds a note to the PDF document on a new page.
     *
     * @param document The PDF document
     * @param note The note to add
     * @param includeMetadata Whether to include metadata
     * @throws IOException If writing to PDF fails
     */
    private void addNoteToDocument(PDDocument document, Note note, boolean includeMetadata) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = page.getMediaBox().getHeight() - AppConstants.PDF_MARGIN;

            yPosition = addNoteTitle(contentStream, note.getTitle(), yPosition);
            yPosition -= AppConstants.PDF_LINE_HEIGHT;

            if (includeMetadata) {
                yPosition = addNoteMetadata(contentStream, note, yPosition);
                yPosition -= AppConstants.PDF_LINE_HEIGHT;
            }

            yPosition = addNoteBody(contentStream, note.getBody(), yPosition, page);
            yPosition = addNoteTags(contentStream, note, yPosition);
            yPosition = addNoteTodos(contentStream, note, yPosition);
            yPosition = addNoteChecklists(contentStream, note, yPosition);
        }
    }

    /**
     * Converts the PDF document to a byte array.
     *
     * @param document The PDF document
     * @return PDF as byte array
     * @throws IOException If conversion fails
     */
    private byte[] convertDocumentToBytes(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }

    /**
     * Adds the note title to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param title The note title
     * @param yPosition Current vertical position
     * @return New vertical position after adding title
     * @throws IOException If writing fails
     */
    private float addNoteTitle(PDPageContentStream contentStream, String title, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, AppConstants.PDF_TITLE_FONT_SIZE);
        contentStream.newLineAtOffset(AppConstants.PDF_MARGIN, yPosition);
        contentStream.showText(title != null ? title : "Untitled");
        contentStream.endText();
        return yPosition - AppConstants.PDF_TITLE_FONT_SIZE - TITLE_VERTICAL_SPACING;
    }

    /**
     * Adds note metadata (created date, category) to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param note The note
     * @param yPosition Current vertical position
     * @return New vertical position after adding metadata
     * @throws IOException If writing fails
     */
    private float addNoteMetadata(PDPageContentStream contentStream, Note note, float yPosition) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String createdAt = note.getCreatedAt() != null ? note.getCreatedAt().format(formatter) : "";
        String category = note.getCategory() != null ? note.getCategory() : "";

        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);

        yPosition = drawTextLine(contentStream, "Created: " + createdAt, yPosition);

        if (!category.isEmpty()) {
            yPosition = drawTextLine(contentStream, "Category: " + category, yPosition);
        }

        return yPosition;
    }

    /**
     * Adds the note body content to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param body The note body
     * @param yPosition Current vertical position
     * @param page The PDF page (for width calculation)
     * @return New vertical position after adding body
     * @throws IOException If writing fails
     */
    private float addNoteBody(PDPageContentStream contentStream, String body, float yPosition, PDPage page) throws IOException {
        if (body == null || body.isEmpty()) {
            return yPosition;
        }

        String cleanBody = stripHtmlTags(body);
        List<String> wrappedLines = wrapText(cleanBody, AppConstants.PDF_WRITABLE_WIDTH, PDType1Font.HELVETICA, AppConstants.PDF_BODY_FONT_SIZE);
        return drawWrappedText(contentStream, wrappedLines, yPosition, AppConstants.PDF_BODY_FONT_SIZE);
    }

    /**
     * Adds note tags to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param note The note
     * @param yPosition Current vertical position
     * @return New vertical position after adding tags
     * @throws IOException If writing fails
     */
    private float addNoteTags(PDPageContentStream contentStream, Note note, float yPosition) throws IOException {
        if (note.getTags() == null || note.getTags().isEmpty()) {
            return yPosition;
        }

        yPosition -= AppConstants.PDF_LINE_HEIGHT;
        yPosition = addSectionHeader(contentStream, "Tags:", yPosition);

        String tagNames = note.getTags().stream()
            .map(Tag::getName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        contentStream.setFont(PDType1Font.HELVETICA, AppConstants.PDF_BODY_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(AppConstants.PDF_MARGIN + CONTENT_INDENT, yPosition);
        contentStream.showText(tagNames);
        contentStream.endText();

        return yPosition - AppConstants.PDF_LINE_HEIGHT;
    }

    /**
     * Adds note todos to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param note The note
     * @param yPosition Current vertical position
     * @return New vertical position after adding todos
     * @throws IOException If writing fails
     */
    private float addNoteTodos(PDPageContentStream contentStream, Note note, float yPosition) throws IOException {
        List<TodoItem> todos = taskRepository.findByNoteId(note.getNoteId());
        if (todos == null || todos.isEmpty()) {
            return yPosition;
        }

        yPosition -= AppConstants.PDF_LINE_HEIGHT;
        yPosition = addSectionHeader(contentStream, "To-Do Items:", yPosition);

        contentStream.setFont(PDType1Font.HELVETICA, AppConstants.PDF_BODY_FONT_SIZE);
        for (TodoItem todo : todos) {
            String status = todo.getStatus() != null ? "[" + todo.getStatus() + "] " : "";
            yPosition = drawIndentedBulletPoint(contentStream, status + todo.getTitle(), yPosition);

            if (yPosition < AppConstants.PDF_MARGIN) {
                break;
            }
        }

        return yPosition;
    }

    /**
     * Adds note checklists to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param note The note
     * @param yPosition Current vertical position
     * @return New vertical position after adding checklists
     * @throws IOException If writing fails
     */
    private float addNoteChecklists(PDPageContentStream contentStream, Note note, float yPosition) throws IOException {
        if (note.getChecklist() == null || note.getChecklist().isEmpty()) {
            return yPosition;
        }

        yPosition -= AppConstants.PDF_LINE_HEIGHT;
        yPosition = addSectionHeader(contentStream, "Checklist:", yPosition);

        contentStream.setFont(PDType1Font.HELVETICA, AppConstants.PDF_BODY_FONT_SIZE);
        for (ChecklistItem item : note.getChecklist()) {
            String checkbox = item.getIsChecked() ? "[X] " : "[ ] ";
            yPosition = drawIndentedText(contentStream, checkbox + item.getText(), yPosition);

            if (yPosition < AppConstants.PDF_MARGIN) {
                break;
            }
        }

        return yPosition;
    }

    /**
     * Adds a section header to the PDF.
     *
     * @param contentStream The PDF content stream
     * @param headerText The header text
     * @param yPosition Current vertical position
     * @return New vertical position after adding header
     * @throws IOException If writing fails
     */
    private float addSectionHeader(PDPageContentStream contentStream, String headerText, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, AppConstants.PDF_SUBTITLE_FONT_SIZE);
        contentStream.beginText();
        contentStream.newLineAtOffset(AppConstants.PDF_MARGIN, yPosition);
        contentStream.showText(headerText);
        contentStream.endText();
        return yPosition - AppConstants.PDF_LINE_HEIGHT;
    }

    /**
     * Draws a single line of text at the current margin.
     *
     * @param contentStream The PDF content stream
     * @param text The text to draw
     * @param yPosition Current vertical position
     * @return New vertical position after drawing
     * @throws IOException If writing fails
     */
    private float drawTextLine(PDPageContentStream contentStream, String text, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(AppConstants.PDF_MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - AppConstants.PDF_LINE_HEIGHT;
    }

    /**
     * Draws indented text with a bullet point.
     *
     * @param contentStream The PDF content stream
     * @param text The text to draw
     * @param yPosition Current vertical position
     * @return New vertical position after drawing
     * @throws IOException If writing fails
     */
    private float drawIndentedBulletPoint(PDPageContentStream contentStream, String text, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(AppConstants.PDF_MARGIN + CONTENT_INDENT, yPosition);
        contentStream.showText("• " + text);
        contentStream.endText();
        return yPosition - AppConstants.PDF_LINE_HEIGHT;
    }

    /**
     * Draws indented text without a bullet point.
     *
     * @param contentStream The PDF content stream
     * @param text The text to draw
     * @param yPosition Current vertical position
     * @return New vertical position after drawing
     * @throws IOException If writing fails
     */
    private float drawIndentedText(PDPageContentStream contentStream, String text, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(AppConstants.PDF_MARGIN + CONTENT_INDENT, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - AppConstants.PDF_LINE_HEIGHT;
    }

    /**
     * Draws multiple lines of wrapped text.
     *
     * @param contentStream The PDF content stream
     * @param lines The lines to draw
     * @param yPosition Current vertical position
     * @param fontSize The font size to use
     * @return New vertical position after drawing all lines
     * @throws IOException If writing fails
     */
    private float drawWrappedText(PDPageContentStream contentStream, List<String> lines, float yPosition, float fontSize) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, fontSize);

        for (String line : lines) {
            contentStream.beginText();
            contentStream.newLineAtOffset(AppConstants.PDF_MARGIN, yPosition);
            contentStream.showText(line);
            contentStream.endText();
            yPosition -= AppConstants.PDF_LINE_HEIGHT;

            if (yPosition < AppConstants.PDF_MARGIN) {
                break;
            }
        }

        return yPosition;
    }

    /**
     * Removes HTML tags from text.
     *
     * @param html The HTML text
     * @return Plain text without HTML tags
     */
    private String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
    }

    /**
     * Wraps text to fit within a specified width.
     *
     * @param text The text to wrap
     * @param maxWidth Maximum width in points
     * @param font The font to use
     * @param fontSize The font size
     * @return List of wrapped text lines
     * @throws IOException If font width calculation fails
     */
    private List<String> wrapText(String text, float maxWidth, PDType1Font font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (textWidth > maxWidth) {
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
