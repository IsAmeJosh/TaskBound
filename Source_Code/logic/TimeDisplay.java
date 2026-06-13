package logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/* Builds the human-readable time-left string shown in the table.
   Depends on TimeParser to convert raw strings into time objects,
   and on fakeToday from the Dev Console so testing works correctly. */
public class TimeDisplay {

    /* Returns a string describing how much time is left before a task
       is due, or how long ago it was due if it has already passed.
       Examples: "2 days left", "3h 15m left", "Overdue by 1 day".
       fakeToday lets the Dev Console simulate a different current date. */
    public static String getTimeLeftDisplay(String dueDate, String dueTime, LocalDate fakeToday) {
        LocalDate due = TimeParser.parseDate(dueDate);
        if (due == null) return "Unknown";

        if (due.isBefore(fakeToday)) {
            /* The due date has already passed, show how many days overdue. */
            long daysOverdue = ChronoUnit.DAYS.between(due, fakeToday);
            return "Overdue by " + daysOverdue + (daysOverdue == 1 ? " day" : " days");
        } else if (due.isEqual(fakeToday)) {
            /* Due today: count down the remaining hours and minutes.
               If no valid due time is set, count down to end of day. */
            LocalTime cutoffTime = TimeParser.parseTime(dueTime);
            if (cutoffTime == null) cutoffTime = LocalTime.MAX;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoff = LocalDateTime.of(fakeToday, cutoffTime);
            long minutesLeft = ChronoUnit.MINUTES.between(now, cutoff);
            if (minutesLeft < 0) minutesLeft = 0;
            long hours = minutesLeft / 60;
            long minutes = minutesLeft % 60;
            return hours + "h " + minutes + "m left";
        } else {
            /* Due date is in the future, show how many days remain. */
            long daysLeft = ChronoUnit.DAYS.between(fakeToday, due);
            return daysLeft + (daysLeft == 1 ? " day left" : " days left");
        }
    }
}