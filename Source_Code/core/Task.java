package core;

/* A single task or assignment entry in the system.
   dueDate is stored as YYYY-MM-DD.
   dueTime is stored and displayed as hh:mm AM/PM.
   See TimeFormatter and FileHandler for conversion logic. */
public class Task {
    public String title;
    public String subject;
    public String dueDate;
    public String dueTime;
    public Status status;
}