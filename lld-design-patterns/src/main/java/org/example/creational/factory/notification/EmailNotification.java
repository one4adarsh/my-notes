package org.example.creational.factory.notification;

public class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("[email] " + message);
    }
}
