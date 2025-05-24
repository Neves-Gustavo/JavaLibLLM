package com.virtupet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;

public class ReminderScheduler {
    private final ConcurrentHashMap<String, Timeline> activeReminders = new ConcurrentHashMap<>();

    public void scheduleDailyReminder(String id, LocalTime time, Runnable action) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(secondsUntilNext(time)),
                        new KeyFrame(Duration.seconds(86400), e -> { // 24h
                            action.run();
                            resetTimer(id, time, action);
                        }).getOnFinished()
                ));
        activeReminders.put(id, timeline);
        timeline.play();
    }
// ReminderScheduler.java Continued

    private double secondsUntilNext(LocalTime target) {
        LocalTime now = LocalTime.now();
        int targetSecond = target.toSecondOfDay();
        int currentSecond = now.toSecondOfDay();
        return (targetSecond > currentSecond) ?
                targetSecond - currentSecond :
                86400 - (currentSecond - targetSecond);
    }

    private void resetTimer(String id, LocalTime time, Runnable action) {
        cancelReminder(id);
        scheduleDailyReminder(id, time, action);
    }

    public void cancelReminder(String id) {
        Timeline timeline = activeReminders.get(id);
        if (timeline != null) {
            timeline.stop();
            activeReminders.remove(id);
        }
    }

    public void updateReminder(String id, LocalTime newTime, Runnable newAction) {
        if (activeReminders.containsKey(id)) {
            cancelReminder(id);
            scheduleDailyReminder(id, newTime, newAction);
        } else {
            throw new IllegalArgumentException("Reminder ID not found: " + id);
        }
    }
}