package org.example.creational.factory.notification;

public final class NotificationFactory {
    private NotificationFactory() {
    }

    public static Notification create(String type) {
        if ("email".equals(type)) {
            return new EmailNotification();
        } else if ("sms".equals(type)) {
            return new SMSNotification();
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
}
