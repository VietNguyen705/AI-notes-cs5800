package com.notesapp.entities;

import com.notesapp.enums.Priority;
import com.notesapp.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "todo_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String taskId;

    @Column(name = "note_id")
    private String noteId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    private LocalDateTime dueDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reminder_id")
    private Reminder reminder;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void update(String title, LocalDateTime dueDate, Priority priority) {
        if (this.status == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update completed task");
        }
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
        this.dueDate = dueDate;
        if (priority != null) {
            this.priority = priority;
        }
    }

    public void complete() {
        if (this.status == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task already completed");
        }
        if (this.status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete cancelled task");
        }
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void updateStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (this.status == TaskStatus.COMPLETED && status == TaskStatus.PENDING) {
            throw new IllegalStateException("Cannot reopen completed task");
        }
        this.status = status;
        if (status == TaskStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void setPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        if (this.status == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change priority of completed task");
        }
        this.priority = priority;
    }

    public void setDueDate(LocalDateTime date) {
        if (this.status == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change due date of completed task");
        }
        if (date != null && date.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        this.dueDate = date;
    }

    public void delete() {
        if (this.reminder != null) {
            this.reminder.cancel();
        }
    }
}
