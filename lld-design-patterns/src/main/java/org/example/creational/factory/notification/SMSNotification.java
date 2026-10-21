package org.example.creational.factory.notification;

public class SMSNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("[sms] " + message);
    }
}
