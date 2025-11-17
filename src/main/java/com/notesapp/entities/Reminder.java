package com.notesapp.entities;

import com.notesapp.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String reminderId;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    private String message;

    @Column(nullable = false)
    private Boolean isDelivered = false;

    @OneToOne(mappedBy = "reminder")
    private Note note;

    @OneToOne(mappedBy = "reminder")
    private TodoItem todoItem;

    public void schedule() {
        if (this.scheduledTime == null || this.scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }
    }

    public void cancel() {
        this.isDelivered = false;
    }

    public void reschedule(LocalDateTime newTime) {
        if (newTime == null || newTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New time must be in the future");
        }
        this.scheduledTime = newTime;
        this.isDelivered = false;
    }

    public void deliver() {
        if (this.isDelivered) {
            throw new IllegalStateException("Reminder already delivered");
        }
        this.isDelivered = true;
    }
}
