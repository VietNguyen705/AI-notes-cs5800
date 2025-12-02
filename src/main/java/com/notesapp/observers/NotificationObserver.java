package com.notesapp.observers;

import com.notesapp.entities.Note;
import org.springframework.stereotype.Component;

/**
 * Observer that sends notifications when notes are created or updated.
 * Could trigger email/push notifications to users about their note activity.
 */
@Component
public class NotificationObserver implements NoteObserver {

    @Override
    public void update(Note note, String eventType) {
        switch (eventType) {
            case "CREATE":
                sendCreationNotification(note);
                break;
            case "UPDATE":
                sendUpdateNotification(note);
                break;
            case "DELETE":
                System.out.println("[Notification] Note deleted: " + note.getTitle());
                break;
            default:
                System.err.println("Unknown event type: " + eventType);
        }
    }

    private void sendCreationNotification(Note note) {
        System.out.println("[Notification] New note created: " + note.getTitle());
    }

    private void sendUpdateNotification(Note note) {
        System.out.println("[Notification] Note updated: " + note.getTitle());
    }
}
