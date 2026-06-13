package core;

// A single task/assignment. dueDate is stored as "YYYY-MM-DD" and dueTime is
// stored/displayed as "hh:mm AM/PM" (see TimeDisplay and FileHandler for the
// conversion logic between formats).
public class Task {
    public String title;
    public String subject;
    public String dueDate;
    public String dueTime;
    public Status status;
}