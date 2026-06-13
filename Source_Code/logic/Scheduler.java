package logic;

import core.Status;
import core.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

// Handles the "automatic" logic around task status: marking tasks as MISSED
// once their due date has passed, and sorting the task list so the most
// relevant tasks show up first.
public class Scheduler {

    // Goes through every task and, if it's still INCOMPLETE but its due date
    // is before "today", flips it to MISSED. Tasks that are already COMPLETE
    // or MISSED are left alone. "today" is passed in rather than read directly
    // so the Dev Console can simulate a different date for testing.
    public static void checkAndMarkMissed(ArrayList<Task> tasks, LocalDate today) {
        for (Task t : tasks) {
            if (t.status == Status.INCOMPLETE) {
                LocalDate due = LocalDate.parse(t.dueDate);
                if (due.isBefore(today)) {
                    t.status = Status.MISSED;
                }
            }
        }
    }

    // Gives each status a sort priority so completed tasks sink to the
    // bottom and missed tasks sink even further, while incomplete (still
    // actionable) tasks stay near the top.
    private static int statusOrder(Status s) {
        if (s == Status.COMPLETE) return 0;
        if (s == Status.INCOMPLETE) return 1;
        return 2; // MISSED
    }

    // Sorts tasks first by status (incomplete tasks first, then complete,
    // then missed), and within each status group sorts by due date so the
    // soonest-due tasks appear first.
    public static void sortByDueDate(ArrayList<Task> tasks) {
        Collections.sort(tasks, (a, b) -> {
            int statusCmp = Integer.compare(statusOrder(a.status), statusOrder(b.status));
            if (statusCmp != 0) return statusCmp;
            return a.dueDate.compareTo(b.dueDate);
        });
    }
}