package org.example.creational.factory;

import org.example.creational.factory.notification.Notification;
import org.example.creational.factory.notification.NotificationFactory;

public class Main {
    public static void main(String[] args) {
        Notification notif = NotificationFactory.create("email");
        notif.send("Hello");

        Notification sms = NotificationFactory.create("sms");
        sms.send("Hello");

        try {
            NotificationFactory.create("fax");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
