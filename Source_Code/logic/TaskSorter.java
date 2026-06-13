package logic;

import core.Status;
import core.Task;
import java.util.ArrayList;
import java.util.Collections;

/* Handles sorting the task list so the most actionable tasks
   appear first. Kept separate from Scheduler because sorting
   and missed-detection are two distinct responsibilities. */
public class TaskSorter {

    /* Assigns a sort priority to each status:
       INCOMPLETE = 0, needs action, shown first
       COMPLETE   = 1, done, shown in the middle
       MISSED     = 2, no longer actionable, shown last */
    private static int statusOrder(Status s) {
        if (s == Status.INCOMPLETE) return 0;
        if (s == Status.COMPLETE) return 1;
        return 2;
    }

    /* Sorts tasks first by status using the order above, then within
       each status group sorts by due date ascending so the soonest-due
       tasks appear first within their group. */
    public static void sort(ArrayList<Task> tasks) {
        Collections.sort(tasks, (a, b) -> {
            int statusCmp = Integer.compare(statusOrder(a.status), statusOrder(b.status));
            if (statusCmp != 0) return statusCmp;
            return a.dueDate.compareTo(b.dueDate);
        });
    }
}