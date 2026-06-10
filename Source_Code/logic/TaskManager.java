package logic;

import core.Task;
import java.util.ArrayList;

public class TaskManager {
    public ArrayList<Task> tasks = new ArrayList<>();

    public void addTask(Task t) {
        tasks.add(t);
    }

    public void printAllTasks() {
        for (Task t : tasks) {
            System.out.println(t.title + " | " + t.subject + " | " + t.dueDate + " | " + t.status);
        }
    }
}