package com.notesapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String tagId;

    @Column(nullable = false, unique = true)
    private String name;

    private String color;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Note> notes = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void attachToNote(Note note) {
        if (note != null) {
            this.notes.add(note);
            note.getTags().add(this);
        }
    }

    public void detachFromNote(Note note) {
        if (note != null) {
            this.notes.remove(note);
            note.getTags().remove(this);
        }
    }
}
