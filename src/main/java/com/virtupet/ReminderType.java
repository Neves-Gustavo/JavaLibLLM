package com.virtupet;
import java.time.LocalTime;

public record ReminderType(
        LocalTime time,
        String message,
        String actionContext
) {}