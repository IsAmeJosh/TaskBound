package logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class TimeDisplay {

    // Returns human readable time-left string
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

    // Parse date in YYYY-MM-DD
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

    // Parse time accepting "HH:mm" or "hh:mm AM/PM"
    public static LocalTime parseTime(String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) return null;
        try {
            String s = dueTime.trim();
            String up = s.toUpperCase();
            if (up.endsWith("AM") || up.endsWith("PM")) {
                String[] parts = s.split("\\s+");
                String timePart = parts[0];
                String ampm = parts[parts.length - 1].toUpperCase();
                String[] hm = timePart.split(":");
                int hh = Integer.parseInt(hm[0].trim());
                int mm = Integer.parseInt(hm[1].trim());
                if (ampm.equals("PM") && hh < 12) hh += 12;
                if (ampm.equals("AM") && hh == 12) hh = 0;
                return LocalTime.of(hh, mm);
            } else {
                String[] hm = s.split(":");
                int hh = Integer.parseInt(hm[0].trim());
                int mm = Integer.parseInt(hm[1].trim());
                return LocalTime.of(hh, mm);
            }
        } catch (Exception e) {
            return null;
        }
    }

    // Format any accepted time into "hh:mm AM/PM"
    public static String formatTo12Hour(String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) return "";
        LocalTime lt = parseTime(dueTime);
        if (lt == null) return dueTime.trim();
        int hour = lt.getHour();
        int minute = lt.getMinute();
        String period = hour >= 12 ? "PM" : "AM";
        int h12 = hour % 12;
        if (h12 == 0) h12 = 12;
        return String.format("%02d:%02d %s", h12, minute, period);
    }
}
