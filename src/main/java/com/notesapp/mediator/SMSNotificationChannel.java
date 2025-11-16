package com.notesapp.mediator;

import org.springframework.stereotype.Component;

/**
 * Mediator Pattern - Concrete Colleague
 *
 * SMS notification channel implementation.
 * Sends text message notifications to mobile phones.
 */
@Component
public class SMSNotificationChannel implements NotificationChannel {

    @Override
    public void send(String message, String recipient) {
        // In a real implementation, this would integrate with an SMS service (e.g., Twilio, AWS SNS)
        System.out.println("💬 [SMS] Sending to " + recipient + ": " + message);

        // Placeholder for actual SMS sending logic
        // Example: twilioService.sendSMS(recipient, message);
    }

    @Override
    public com.notesapp.enums.NotificationChannel getType() {
        return com.notesapp.enums.NotificationChannel.SMS;
    }
}
