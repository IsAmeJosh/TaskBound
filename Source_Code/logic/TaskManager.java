package logic;

import core.Task;
import core.Status;
import java.util.ArrayList;

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

    public void printAllTasks() {
        for (Task t : tasks) {
            System.out.println(t.title + " | " + t.subject + " | " + t.dueDate + " | " + t.status);
        }
    }
}