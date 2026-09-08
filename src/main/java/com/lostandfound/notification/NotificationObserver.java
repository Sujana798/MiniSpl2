package com.lostandfound.notification;

public interface NotificationObserver {
    void onNotify(int userId, String message);
}