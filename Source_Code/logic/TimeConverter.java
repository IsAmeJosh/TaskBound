package logic;

/* Handles all conversion between 12hr and 24hr time formats.
   This is kept separate from TimeParser and TimeDisplay so each
   file has exactly one job. FileHandler uses to24Hour for storage,
   and the UI uses to12Hour and formatTo12Hour for display. */
public class TimeConverter {

    /* Converts a hh:mm AM/PM string into HH:mm for CSV storage.
       If the string does not end in AM or PM, it is assumed to
       already be in 24hr format and is returned as is. */
    public static String to24Hour(String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) return "";
        String s = dueTime.trim();
        String up = s.toUpperCase();
        if (!up.endsWith("AM") && !up.endsWith("PM")) return s;
        try {
            String[] tok = s.split("\\s+");
            String timePart = tok[0];
            String ampm = tok[tok.length - 1].toUpperCase();
            String[] hm = timePart.split(":");
            int hh = Integer.parseInt(hm[0].trim());
            int mm = Integer.parseInt(hm[1].trim());
            if (ampm.equals("PM") && hh < 12) hh += 12;
            if (ampm.equals("AM") && hh == 12) hh = 0;
            return String.format("%02d:%02d", hh, mm);
        } catch (Exception e) {
            return s;
        }
    }

    /* Converts any parseable time string into hh:mm AM/PM for display.
       Accepts both HH:mm and hh:mm AM/PM as input.
       If the string cannot be parsed, it is returned untouched
       so no data is silently lost. */
    public static String formatTo12Hour(String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) return "";
        java.time.LocalTime lt = TimeParser.parseTime(dueTime);
        if (lt == null) return dueTime.trim();
        int hour = lt.getHour();
        int minute = lt.getMinute();
        String period = hour >= 12 ? "PM" : "AM";
        int h12 = hour % 12;
        if (h12 == 0) h12 = 12;
        return String.format("%02d:%02d %s", h12, minute, period);
    }

    /* Goes through every task in the list and rewrites its dueTime
       to the standard hh:mm AM/PM format. Called after loading from
       a file or syncing from the LMS, since those sources may use 24hr. */
    public static void normalizeAllTimes(java.util.ArrayList<core.Task> tasks) {
        if (tasks == null) return;
        for (core.Task t : tasks) {
            if (t != null) t.dueTime = formatTo12Hour(t.dueTime);
        }
    }
}