package logic;

import core.Task;
import java.util.ArrayList;
import javax.swing.JComboBox;

/* Manages the Subject filter dropdown specifically.
   Kept separate from TaskFilter because rebuilding the dropdown
   options is its own distinct job from filtering the task list. */
public class SubjectFilter {

    /* Rebuilds the list of subjects shown in the subject filter dropdown
       based on whatever subjects currently exist across all tasks.
       Tries to keep the previously selected subject selected if it still
       exists, otherwise falls back to All. */
    public static void refreshSubjectFilterOptions(
            ArrayList<Task> tasks,
            JComboBox<String> subjectFilterBox) {

        String previous = (String) subjectFilterBox.getSelectedItem();
        subjectFilterBox.removeAllItems();
        subjectFilterBox.addItem("All");

        /* LinkedHashSet keeps subjects in the order they are first seen
           with no duplicates, so the dropdown order stays stable. */
        java.util.LinkedHashSet<String> subjects = new java.util.LinkedHashSet<>();
        for (Task t : tasks) {
            if (t.subject != null && !t.subject.trim().isEmpty()) {
                subjects.add(t.subject);
            }
        }
        for (String s : subjects) {
            subjectFilterBox.addItem(s);
        }

        /* Try to restore the previous selection.
           If it no longer exists in the list, default to All. */
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