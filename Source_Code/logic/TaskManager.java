package logic;

import core.Status;
import core.Task;
import java.util.ArrayList;

/* Holds the master list of all tasks and provides basic operations
   on it. This is the single shared source of truth that all UI
   panels read from and write to during a session. */
public class TaskManager {
    public ArrayList<Task> tasks = new ArrayList<>();

    /* Adds a new task to the end of the list. */
    public void addTask(Task t) {
        tasks.add(t);
    }

    /* Removes the task at the given index if the index is valid. */
    public void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
        }
    }

    /* Sets the task at the given index to COMPLETE if the index is valid. */
    public void markComplete(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).status = Status.COMPLETE;
        }
    }

    /* Returns the full unfiltered task list. */
    public ArrayList<Task> getAllTasks() {
        return tasks;
    }

    /* Prints all tasks to the console. Used for quick debugging. */
    public void printAllTasks() {
        for (Task t : tasks) {
            System.out.println(t.title + " | " + t.subject + " | " + t.dueDate + " | " + t.status);
        }
    }
}