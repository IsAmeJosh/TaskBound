package logic;

import core.Status;
import core.Task;
import java.time.LocalDate;
import java.util.ArrayList;

/* Handles automatic task status updates. Specifically, it detects
   tasks whose due date has passed while still INCOMPLETE and flips
   them to MISSED. Sorting is handled separately by TaskSorter. */
public class Scheduler {

    /* Goes through every task and flips any INCOMPLETE task whose due
       date is before today to MISSED. Already COMPLETE or MISSED tasks
       are left alone. today is passed in so the Dev Console can simulate
       a different date for testing without affecting real system time.
       Returns the list of tasks that just became MISSED during this call
       so callers can show a notification without re-notifying old ones. */
    public static ArrayList<Task> checkAndMarkMissed(ArrayList<Task> tasks, LocalDate today) {
        ArrayList<Task> newlyMissed = new ArrayList<>();
        for (Task t : tasks) {
            if (t.status == Status.INCOMPLETE) {
                LocalDate due = LocalDate.parse(t.dueDate);
                if (due.isBefore(today)) {
                    t.status = Status.MISSED;
                    newlyMissed.add(t);
                }
            }
        }
        return newlyMissed;
    }
}