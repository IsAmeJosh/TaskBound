package logic;

import core.Status;
import core.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class Scheduler {

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

    // Returns sort priority: COMPLETE=0, INCOMPLETE=1, MISSED=2
    private static int statusOrder(Status s) {
        if (s == Status.COMPLETE) return 0;
        if (s == Status.INCOMPLETE) return 1;
        return 2; // MISSED
    }

    public static void sortByDueDate(ArrayList<Task> tasks) {
        Collections.sort(tasks, (a, b) -> {
            int statusCmp = Integer.compare(statusOrder(a.status), statusOrder(b.status));
            if (statusCmp != 0) return statusCmp;
            return a.dueDate.compareTo(b.dueDate);
        });
    }
}