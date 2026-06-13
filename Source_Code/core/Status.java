package core;

/* The three possible states a task can be in.
   MISSED is set automatically by Scheduler when an INCOMPLETE task's
   due date passes. Users can also set it manually via Change Status. */
public enum Status {
    INCOMPLETE, COMPLETE, MISSED
}