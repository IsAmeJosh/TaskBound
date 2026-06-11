package logic;

import core.Task;
import core.Status;
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

    public static void sortByDueDate(ArrayList<Task> tasks) {
        Collections.sort(tasks, (a, b) -> a.dueDate.compareTo(b.dueDate));
    }
}