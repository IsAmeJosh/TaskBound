package logic;

import core.Task;
import java.util.ArrayList;

public class LMSMockData {

    public static ArrayList<Task> getFakeTasks() {
        ArrayList<Task> fakeTasks = new ArrayList<>();

        Task t1 = new Task();
        t1.title = "Blackboard Quiz 1";
        t1.subject = "IT101";
        t1.dueDate = "2026-06-20";
        t1.status = "INCOMPLETE";

        Task t2 = new Task();
        t2.title = "Canvas Assignment 2";
        t2.subject = "Math102";
        t2.dueDate = "2026-06-22";
        t2.status = "INCOMPLETE";

        Task t3 = new Task();
        t3.title = "Lab Report";
        t3.subject = "Science103";
        t3.dueDate = "2026-06-25";
        t3.status = "INCOMPLETE";

        fakeTasks.add(t1);
        fakeTasks.add(t2);
        fakeTasks.add(t3);

        return fakeTasks;
    }
}