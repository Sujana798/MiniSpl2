package com.lostandfound.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationPublisher {

    private static NotificationPublisher instance;
    private final List<NotificationObserver> observers = new ArrayList<>();

    private NotificationPublisher() {
    }

    public static NotificationPublisher getInstance() {
        if (instance == null) {
            instance = new NotificationPublisher();
        }
        return instance;
    }

    public void subscribe(NotificationObserver observer) {
        observers.add(observer);
    }

    public void publish(int userId, String message) {
        for (NotificationObserver observer : observers) {
            observer.onNotify(userId, message);
        }
    }
}