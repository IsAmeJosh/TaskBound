package core;

// The three possible states a task can be in. MISSED is set automatically by
// Scheduler when an INCOMPLETE task's due date passes - it's never chosen by
// the user directly except via the Change Status dialog for manual overrides.
public enum Status {
    INCOMPLETE, COMPLETE, MISSED
}