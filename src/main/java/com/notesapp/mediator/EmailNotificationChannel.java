package com.notesapp.mediator;

import org.springframework.stereotype.Component;

/**
 * Mediator Pattern - Concrete Colleague
 *
 * Email notification channel implementation.
 * Sends notifications via email to the recipient.
 */
@Component
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public void send(String message, String recipient) {
        // In a real implementation, this would integrate with an email service (e.g., SendGrid, AWS SES)
        System.out.println("📧 [EMAIL] Sending to " + recipient + ": " + message);

        // Placeholder for actual email sending logic
        // Example: emailService.sendEmail(recipient, "Reminder Notification", message);
    }

    @Override
    public com.notesapp.enums.NotificationChannel getType() {
        return com.notesapp.enums.NotificationChannel.EMAIL;
    }
}
