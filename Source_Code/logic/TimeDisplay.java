package logic;

import core.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

// This class handles everything related to dates and times: parsing them,
// converting between 12hr/24hr formats, and figuring out how much time is left
// before a task is due. Keeping all of this in one place means we only have
// one source of truth for "what does this date/time string actually mean".
public class TimeDisplay {

    // Builds the "time left" text shown in the table, e.g. "2 days left",
    // "3h 15m left", or "Overdue by 1 day". fakeToday lets the Dev Console
    // simulate a different "current date" for testing.
    public static String getTimeLeftDisplay(String dueDate, String dueTime, LocalDate fakeToday) {
        LocalDate due = parseDate(dueDate);
        if (due == null) return "Unknown";

        if (due.isBefore(fakeToday)) {
            // The due date has already passed, so show how many days overdue.
            long daysOverdue = ChronoUnit.DAYS.between(due, fakeToday);
            return "Overdue by " + daysOverdue + (daysOverdue == 1 ? " day" : " days");
        } else if (due.isEqual(fakeToday)) {
            // Due today: count down the remaining hours and minutes until the cutoff time.
            // If no valid due time is set, fall back to counting down to the end of the day.
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
            // Due date is in the future, just show how many days are left.
            long daysLeft = ChronoUnit.DAYS.between(fakeToday, due);
            return daysLeft + (daysLeft == 1 ? " day left" : " days left");
        }
    }

    // Parses a "YYYY-MM-DD" string into a LocalDate. Returns null if the
    // string is missing or malformed so callers can handle bad data gracefully.
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

    // Parses a time string that could be either "HH:mm" (24hr) or
    // "hh:mm AM/PM" (12hr) and returns a LocalTime. Returns null if the
    // string is empty or can't be understood.
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

    // Takes any time string we can parse and reformats it as "hh:mm AM/PM".
    // If we can't parse it at all, just return the original text untouched
    // rather than losing the user's data.
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

    // Same idea as formatTo12Hour, but this is the version that was previously
    // duplicated inside TasksPanel. Moved here so all time-formatting logic
    // lives in one place. Functionally identical to formatTo12Hour, kept as
    // an alias so existing calls (to12Hour) keep working without confusion.
    public static String to12Hour(String time) {
        return formatTo12Hour(time);
    }

    // Goes through every task in the list and rewrites its dueTime to the
    // standard "hh:mm AM/PM" format. Useful right after loading tasks from
    // a file or syncing from the LMS, since those sources might use 24hr time.
    public static void normalizeAllTimes(ArrayList<Task> tasks) {
        if (tasks == null) return;
        for (Task t : tasks) {
            if (t != null) t.dueTime = formatTo12Hour(t.dueTime);
        }
    }
}