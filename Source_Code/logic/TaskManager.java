package logic;

import core.Status;
import core.Task;
import java.util.ArrayList;

// Holds the master list of all tasks and provides basic add/delete/complete
// operations. This is the single shared source of truth that all the UI
// panels read from and write to.
public class TaskManager {
    public ArrayList<Task> tasks = new ArrayList<>();

    public void addTask(Task t) {
        tasks.add(t);
    }

    public void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
        }
    }

    public void markComplete(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).status = Status.COMPLETE;
        }
    }

    public ArrayList<Task> getAllTasks() {
        return tasks;
    }

    // Quick debug helper for printing the current task list to the console.
    public void printAllTasks() {
        for (Task t : tasks) {
            System.out.println(t.title + " | " + t.subject + " | " + t.dueDate + " | " + t.status);
        }
    }
}