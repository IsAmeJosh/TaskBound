package logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class TimeDisplay {

    public static String getTimeLeftDisplay(String dueDate, String dueTime, LocalDate fakeToday) {
        LocalDate due = parseDate(dueDate);
        if (due == null) return "Unknown";

        if (due.isBefore(fakeToday)) {
            long daysOverdue = ChronoUnit.DAYS.between(due, fakeToday);
            return "Overdue by " + daysOverdue + (daysOverdue == 1 ? " day" : " days");
        } else if (due.isEqual(fakeToday)) {
            LocalTime cutoffTime = parseTime(dueTime);
            if (cutoffTime == null) cutoffTime = LocalTime.MAX;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoff = LocalDateTime.of(fakeToday, cutoffTime);
            long minutesLeft = ChronoUnit.MINUTES.between(now, cutoff);
            if (minutesLeft < 0) minutesLeft = 0;
            long hours = minutesLeft / 60;
            long minutes = minutesLeft % 60;
            return hours + "h " + minutes + "m left";
        } else {
            long daysLeft = ChronoUnit.DAYS.between(fakeToday, due);
            return daysLeft + (daysLeft == 1 ? " day left" : " days left");
        }
    }

    public static LocalDate parseDate(String dueDate) {
        if (dueDate == null) return null;
        try {
            String[] parts = dueDate.trim().split("-");
            if (parts.length != 3) return null;
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalTime parseTime(String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) return null;
        try {
            String[] parts = dueTime.trim().split(":");
            if (parts.length != 2) return null;
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return null;
        }
    }
}