package com.lostandfound.notification;

import com.lostandfound.dao.NotificationDao;

public class InAppNotificationObserver implements NotificationObserver {

    private final NotificationDao notificationDao;

    public InAppNotificationObserver(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public void onNotify(int userId, String message) {
        notificationDao.insertNotification(userId, message);
    }
}