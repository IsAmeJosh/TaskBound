package logic;

import core.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.JComboBox;

/* Handles filtering the task list based on the Subject and Due
   dropdowns. Returns only the tasks that match both filters.
   If a filter is set to All, that filter is skipped entirely. */
public class TaskFilter {

    /* Returns only the tasks matching both the subject filter and
       the due-date category filter currently selected in the dropdowns.
       If either dropdown is null, that filter is treated as All. */
    public static ArrayList<Task> getFilteredTasks(
            ArrayList<Task> tasks,
            JComboBox<String> subjectFilterBox,
            JComboBox<String> statusFilterBox,
            LocalDate fakeToday) {

        ArrayList<Task> result = new ArrayList<>();
        String subjectFilter = subjectFilterBox != null ? (String) subjectFilterBox.getSelectedItem() : "All";
        String dueFilter = statusFilterBox != null ? (String) statusFilterBox.getSelectedItem() : "All";

        for (Task t : tasks) {
            /* Skip this task if it does not match the chosen subject. */
            if (subjectFilter != null && !subjectFilter.equals("All")) {
                if (t.subject == null || !t.subject.equals(subjectFilter)) continue;
            }
            /* Skip this task if it does not fall into the chosen due category. */
            if (dueFilter != null && !dueFilter.equals("All")) {
                String category = getDueCategory(t.dueDate, fakeToday);
                if (!dueFilter.equals(category)) continue;
            }
            result.add(t);
        }
        return result;
    }

    /* Works out whether a task's due date is Overdue, Due Today, or
       Upcoming relative to fakeToday. Returns Unknown if the date
       string cannot be parsed. */
    public static String getDueCategory(String dueDate, LocalDate fakeToday) {
        LocalDate due = TimeParser.parseDate(dueDate);
        if (due == null) return "Unknown";
        if (due.isBefore(fakeToday)) return "Overdue";
        else if (due.isEqual(fakeToday)) return "Due Today";
        else return "Upcoming";
    }
}