package com.lostandfound.notification;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationPublisherTest {

    @Test
    void publish_shouldNotifyAllSubscribedObservers() {
        NotificationPublisher publisher = NotificationPublisher.getInstance();

        List<String> receivedMessages = new ArrayList<>();
        NotificationObserver testObserver = (userId, message) -> receivedMessages.add(message);

        publisher.subscribe(testObserver);
        publisher.publish(1, "Test notification message");

        assertTrue(receivedMessages.contains("Test notification message"));
    }

    @Test
    void publish_shouldNotifyMultipleObservers() {
        NotificationPublisher publisher = NotificationPublisher.getInstance();

        List<String> observerA = new ArrayList<>();
        List<String> observerB = new ArrayList<>();

        publisher.subscribe((userId, message) -> observerA.add(message));
        publisher.subscribe((userId, message) -> observerB.add(message));

        publisher.publish(2, "Broadcast message");

        assertTrue(observerA.contains("Broadcast message"));
        assertTrue(observerB.contains("Broadcast message"));
    }
}