package logic;

import core.Status;
import core.Task;
import java.util.ArrayList;

// Stands in for a real LMS (Learning Management System) connection. Hitting
// "Sync LMS" calls getFakeTasks() to pretend we just pulled a fresh task list
// from Canvas/Blackboard. Replace this with a real API call later without
// having to change anything else in the app - TasksPanel just calls
// getFakeTasks() and doesn't care where the data comes from.
public class LMSMockData {

    // Returns a hardcoded set of sample tasks covering each subject, with a
    // mix of due dates/times so overdue, due-today, and upcoming filters all
    // have something to show.
    public static ArrayList<Task> getFakeTasks() {
        ArrayList<Task> fakeTasks = new ArrayList<>();

        Task t1 = new Task();
        t1.title = "Blackboard Quiz 1";
        t1.subject = "IT101-2";
        t1.dueDate = "2026-06-20";
        t1.dueTime = "23:59";
        t1.status = Status.INCOMPLETE;

        Task t2 = new Task();
        t2.title = "Lab Activity 3";
        t2.subject = "IT101-2L";
        t2.dueDate = "2026-06-22";
        t2.dueTime = "23:59";
        t2.status = Status.INCOMPLETE;

        Task t3 = new Task();
        t3.title = "HCI Case Study";
        t3.subject = "CS152";
        t3.dueDate = "2026-06-25";
        t3.dueTime = "12:00";
        t3.status = Status.INCOMPLETE;

        Task t4 = new Task();
        t4.title = "Life Coaching Reflection";
        t4.subject = "VE023";
        t4.dueDate = "2026-06-18";
        t4.dueTime = "23:59";
        t4.status = Status.INCOMPLETE;

        Task t5 = new Task();
        t5.title = "MMW Problem Set";
        t5.subject = "MATH035";
        t5.dueDate = "2026-06-15";
        t5.dueTime = "23:59";
        t5.status = Status.INCOMPLETE;

        Task t6 = new Task();
        t6.title = "Fitness Log Submission";
        t6.subject = "PE003";
        t6.dueDate = "2026-06-12";
        t6.dueTime = "20:00";
        t6.status = Status.INCOMPLETE;

        Task t7 = new Task();
        t7.title = "PIIS Research Paper";
        t7.subject = "IS102";
        t7.dueDate = "2026-06-28";
        t7.dueTime = "23:59";
        t7.status = Status.INCOMPLETE;

        Task t8 = new Task();
        t8.title = "Essay 2 Draft";
        t8.subject = "ENG041";
        t8.dueDate = "2026-06-19";
        t8.dueTime = "23:59";
        t8.status = Status.INCOMPLETE;

        Task t9 = new Task();
        t9.title = "Contemporary World Quiz";
        t9.subject = "SS023";
        t9.dueDate = "2026-06-30";
        t9.dueTime = "23:59";
        t9.status = Status.INCOMPLETE;

        fakeTasks.add(t1);
        fakeTasks.add(t2);
        fakeTasks.add(t3);
        fakeTasks.add(t4);
        fakeTasks.add(t5);
        fakeTasks.add(t6);
        fakeTasks.add(t7);
        fakeTasks.add(t8);
        fakeTasks.add(t9);

        return fakeTasks;
    }
}