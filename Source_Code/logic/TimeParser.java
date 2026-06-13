package logic;

import java.time.LocalDate;
import java.time.LocalTime;

/* Parses raw date and time strings into Java time objects.
   Kept separate from TimeConverter and TimeDisplay so parsing
   logic has one clear home and can be reused by both. */
public class TimeParser {

    /* Parses a YYYY-MM-DD string into a LocalDate.
       Returns null if the string is missing or malformed
       so callers can handle bad data gracefully. */
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

    /* Parses a time string that can be either HH:mm in 24hr
       or hh:mm AM/PM in 12hr and returns a LocalTime.
       Returns null if the string is empty or cannot be understood. */
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
}