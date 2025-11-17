package com.notesapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(exclude = {"user", "tags"})
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String noteId;

    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false)
    private String title;

    @Column(length = 10000)
    private String body;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChecklistItem> checklist = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "note_images", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    private String voiceRecording;

    private String color;

    private String category;

    @Column(name = "category_id")
    private String categoryId;

    @Column(nullable = false)
    private Boolean isPinned = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "note_tags",
        joinColumns = @JoinColumn(name = "note_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reminder_id")
    private Reminder reminder;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void create() {
        if (this.title == null || this.title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }

    public void update(Map<String, Object> content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        if (content.containsKey("title")) {
            this.title = (String) content.get("title");
        }
        if (content.containsKey("body")) {
            this.body = (String) content.get("body");
        }
        if (content.containsKey("color")) {
            this.color = (String) content.get("color");
        }
        if (content.containsKey("category")) {
            this.category = (String) content.get("category");
        }
        if (content.containsKey("isPinned")) {
            this.isPinned = (Boolean) content.get("isPinned");
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.reminder != null) {
            this.reminder.cancel();
        }
    }

    public void addTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        this.tags.add(tag);
        tag.getNotes().add(this);
    }

    public void removeTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        this.tags.remove(tag);
        tag.getNotes().remove(this);
    }

    public void autoOrganize() {
    }

    public List<TodoItem> generateTasks() {
        return new ArrayList<>();
    }

    public void setReminder(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null");
        }
        if (reminder.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reminder time must be in the future");
        }
        this.reminder = reminder;
        reminder.setNote(this);
    }
}
