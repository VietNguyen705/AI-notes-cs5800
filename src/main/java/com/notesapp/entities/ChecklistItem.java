package com.notesapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "checklist_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String itemId;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Boolean isChecked = false;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private Note note;

    public void toggle() {
        this.isChecked = !this.isChecked;
    }
}
