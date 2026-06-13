package logic;

import core.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.JComboBox;

// Handles the "Subject" and "Due" filter dropdowns: figuring out which tasks
// match the current filter selections, and keeping the subject dropdown's
// list of options up to date as tasks get added/removed/synced.
public class TaskFilter {

    // Returns only the tasks that match both the subject filter and the
    // due-date filter currently selected in the two dropdowns. If a dropdown
    // is null or set to "All", that filter is skipped entirely.
    public static ArrayList<Task> getFilteredTasks(
            ArrayList<Task> tasks,
            JComboBox<String> subjectFilterBox,
            JComboBox<String> statusFilterBox,
            LocalDate fakeToday) {

        ArrayList<Task> result = new ArrayList<>();
        String subjectFilter = subjectFilterBox != null ? (String) subjectFilterBox.getSelectedItem() : "All";
        String dueFilter = statusFilterBox != null ? (String) statusFilterBox.getSelectedItem() : "All";

        for (Task t : tasks) {
            // Skip this task if it doesn't match the chosen subject.
            if (subjectFilter != null && !subjectFilter.equals("All")) {
                if (t.subject == null || !t.subject.equals(subjectFilter)) continue;
            }
            // Skip this task if it doesn't fall into the chosen due-date category.
            if (dueFilter != null && !dueFilter.equals("All")) {
                String category = getDueCategory(t.dueDate, fakeToday);
                if (!dueFilter.equals(category)) continue;
            }
            result.add(t);
        }
        return result;
    }

    // Works out whether a task's due date is "Overdue", "Due Today", or
    // "Upcoming" relative to fakeToday. Returns "Unknown" if the date string
    // can't be parsed at all.
    public static String getDueCategory(String dueDate, LocalDate fakeToday) {
        LocalDate due = TimeDisplay.parseDate(dueDate);
        if (due == null) return "Unknown";
        if (due.isBefore(fakeToday)) return "Overdue";
        else if (due.isEqual(fakeToday)) return "Due Today";
        else return "Upcoming";
    }

    // Rebuilds the list of subjects shown in the subject filter dropdown,
    // based on whatever subjects currently exist across all tasks. Tries to
    // keep the user's previously-selected subject selected if it still exists,
    // otherwise falls back to "All".
    public static void refreshSubjectFilterOptions(
            ArrayList<Task> tasks,
            JComboBox<String> subjectFilterBox) {

        String previous = (String) subjectFilterBox.getSelectedItem();
        subjectFilterBox.removeAllItems();
        subjectFilterBox.addItem("All");

        // LinkedHashSet keeps subjects in the order they're first seen, with
        // no duplicates - so the dropdown order stays stable and predictable.
        java.util.LinkedHashSet<String> subjects = new java.util.LinkedHashSet<>();
        for (Task t : tasks) {
            if (t.subject != null && !t.subject.trim().isEmpty()) {
                subjects.add(t.subject);
            }
        }
        for (String s : subjects) {
            subjectFilterBox.addItem(s);
        }

        // Try to restore the previous selection; if it's gone, default to "All".
        if (previous != null) {
            for (int i = 0; i < subjectFilterBox.getItemCount(); i++) {
                if (subjectFilterBox.getItemAt(i).equals(previous)) {
                    subjectFilterBox.setSelectedItem(previous);
                    return;
                }
            }
        }
        subjectFilterBox.setSelectedIndex(0);
    }
}