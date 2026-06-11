package logic;

import core.Task;
import core.Status;
import java.util.ArrayList;

public class LMSMockData {

    public static ArrayList<Task> getFakeTasks() {
        ArrayList<Task> fakeTasks = new ArrayList<>();

        Task t1 = new Task();
        t1.title = "Blackboard Quiz 1";
        t1.subject = "IT101";
        t1.dueDate = "2026-06-20";
        t1.dueTime = "11:59 PM";
        t1.status = Status.INCOMPLETE;

        Task t2 = new Task();
        t2.title = "Canvas Assignment 2";
        t2.subject = "Math102";
        t2.dueDate = "2026-06-22";
        t2.dueTime = "11:59 PM";
        t2.status = Status.INCOMPLETE;

        Task t3 = new Task();
        t3.title = "Lab Report";
        t3.subject = "Science103";
        t3.dueDate = "2026-06-25";
        t3.dueTime = "08:00 AM";
        t3.status = Status.INCOMPLETE;

        Task t4 = new Task();
        t4.title = "Programming Exercise 1";
        t4.subject = "IT102";
        t4.dueDate = "2026-07-01";
        t4.dueTime = "05:00 PM";
        t4.status = Status.INCOMPLETE;

        Task t5 = new Task();
        t5.title = "Essay Draft";
        t5.subject = "English101";
        t5.dueDate = "2026-07-05";
        t5.dueTime = "11:59 PM";
        t5.status = Status.INCOMPLETE;

        fakeTasks.add(t1);
        fakeTasks.add(t2);
        fakeTasks.add(t3);
        fakeTasks.add(t4);
        fakeTasks.add(t5);

        return fakeTasks;
    }
}