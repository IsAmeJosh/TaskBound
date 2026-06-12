package logic;

import core.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.JComboBox;

public class TaskFilter {

    public static ArrayList<Task> getFilteredTasks(
            ArrayList<Task> tasks,
            JComboBox<String> subjectFilterBox,
            JComboBox<String> statusFilterBox,
            LocalDate fakeToday) {

        ArrayList<Task> result = new ArrayList<>();
        String subjectFilter = subjectFilterBox != null ? (String) subjectFilterBox.getSelectedItem() : "All";
        String dueFilter = statusFilterBox != null ? (String) statusFilterBox.getSelectedItem() : "All";

        for (Task t : tasks) {
            if (subjectFilter != null && !subjectFilter.equals("All")) {
                if (t.subject == null || !t.subject.equals(subjectFilter)) continue;
            }
            if (dueFilter != null && !dueFilter.equals("All")) {
                String category = getDueCategory(t.dueDate, fakeToday);
                if (!dueFilter.equals(category)) continue;
            }
            result.add(t);
        }
        return result;
    }

    public static String getDueCategory(String dueDate, LocalDate fakeToday) {
        LocalDate due = TimeDisplay.parseDate(dueDate);
        if (due == null) return "Unknown";
        if (due.isBefore(fakeToday)) return "Overdue";
        else if (due.isEqual(fakeToday)) return "Due Today";
        else return "Upcoming";
    }

    public static void refreshSubjectFilterOptions(
            ArrayList<Task> tasks,
            JComboBox<String> subjectFilterBox) {

        String previous = (String) subjectFilterBox.getSelectedItem();
        subjectFilterBox.removeAllItems();
        subjectFilterBox.addItem("All");

        java.util.LinkedHashSet<String> subjects = new java.util.LinkedHashSet<>();
        for (Task t : tasks) {
            if (t.subject != null && !t.subject.trim().isEmpty()) {
                subjects.add(t.subject);
            }
        }
        for (String s : subjects) {
            subjectFilterBox.addItem(s);
        }

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