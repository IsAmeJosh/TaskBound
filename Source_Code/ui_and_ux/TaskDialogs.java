package ui_and_ux;

import core.Status;
import core.Task;
import java.awt.*;
import javax.swing.*;
import logic.TimeDisplay;

// This class is home to all the popup dialogs used to create or edit tasks.
// Pulling these out of TasksPanel keeps that file focused on just the screen
// layout, while this file focuses purely on "build a form, show it, hand
// back what the user entered".
public class TaskDialogs {

    // Shared list of options used by the date/time pickers in both dialogs below.
    private static final String[] YEARS = {"2026", "2027", "2028"};
    private static final String[] MONTHS = {"01","02","03","04","05","06","07","08","09","10","11","12"};
    private static final String[] HOURS_12 = {"12","01","02","03","04","05","06","07","08","09","10","11"};
    private static final String[] MINUTES = {"00","15","30","45"};
    private static final String[] AMPM = {"AM","PM"};

    // Builds the list of day numbers 01-31 for the day dropdown.
    private static String[] buildDays() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
        return days;
    }

    // Shows the "Add Task" popup. Returns a fully-filled Task if the user
    // confirms with valid input, or null if they cancel or leave required
    // fields empty (an error message is shown in the empty-field case).
    public static Task showAddTaskDialog(JFrame frame) {
        JTextField titleField = new JTextField();
        JTextField subjectField = new JTextField();

        JComboBox<String> yearBox = new JComboBox<>(YEARS);
        JComboBox<String> monthBox = new JComboBox<>(MONTHS);
        JComboBox<String> dayBox = new JComboBox<>(buildDays());
        JComboBox<String> hourBox = new JComboBox<>(HOURS_12);
        JComboBox<String> minuteBox = new JComboBox<>(MINUTES);
        JComboBox<String> ampmBox = new JComboBox<>(AMPM);
        // None of these dropdowns need to show a focus ring after a selection,
        // so we turn focusability off on all of them.
        yearBox.setFocusable(false);
        monthBox.setFocusable(false);
        dayBox.setFocusable(false);
        hourBox.setFocusable(false);
        minuteBox.setFocusable(false);
        ampmBox.setFocusable(false);

        JPanel datePanel = new JPanel();
        datePanel.add(yearBox); datePanel.add(new JLabel("-"));
        datePanel.add(monthBox); datePanel.add(new JLabel("-"));
        datePanel.add(dayBox);

        JPanel timePanel = new JPanel();
        timePanel.add(hourBox); timePanel.add(new JLabel(":"));
        timePanel.add(minuteBox); timePanel.add(ampmBox);

        Object[] fields = {
            "Task:", titleField,
            "Subject:", subjectField,
            "Due Date:", datePanel,
            "Due Time:", timePanel
        };

        int res = JOptionPane.showConfirmDialog(frame, fields, "Add Task", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return null;

        String title = titleField.getText().trim();
        String subject = subjectField.getText().trim();
        if (title.isEmpty() || subject.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Task and Subject cannot be empty.");
            return null;
        }

        // New tasks always start as INCOMPLETE since they haven't been worked on yet.
        Task t = new Task();
        t.title = title;
        t.subject = subject;
        t.dueDate = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
        String dueTime = hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + " " + ampmBox.getSelectedItem();
        t.dueTime = TimeDisplay.formatTo12Hour(dueTime);
        t.status = Status.INCOMPLETE;
        return t;
    }

    // Shows the "Change Status" popup, pre-filled with the selected task's
    // current status, due date, and due time. If the user confirms, the
    // task object is updated in place and this returns true. Returns false
    // if the user cancels (in which case the task is left untouched).
    public static boolean showEditTaskDialog(JFrame frame, Task sel) {
        Status[] options = {Status.INCOMPLETE, Status.COMPLETE, Status.MISSED};
        JComboBox<Status> statusBox = new JComboBox<>(options);
        statusBox.setSelectedItem(sel.status);
        statusBox.setFocusable(false);

        JComboBox<String> yearBox = new JComboBox<>(YEARS);
        JComboBox<String> monthBox = new JComboBox<>(MONTHS);
        JComboBox<String> dayBox = new JComboBox<>(buildDays());
        yearBox.setFocusable(false);
        monthBox.setFocusable(false);
        dayBox.setFocusable(false);

        // Pre-fill the date pickers with the task's existing due date, if it's valid.
        if (sel.dueDate != null && sel.dueDate.contains("-")) {
            String[] p = sel.dueDate.split("-");
            if (p.length == 3) {
                yearBox.setSelectedItem(p[0]);
                monthBox.setSelectedItem(p[1]);
                dayBox.setSelectedItem(p[2]);
            }
        }

        JComboBox<String> hourBox = new JComboBox<>(HOURS_12);
        JComboBox<String> minuteBox = new JComboBox<>(MINUTES);
        JComboBox<String> ampmBox = new JComboBox<>(AMPM);
        hourBox.setFocusable(false);
        minuteBox.setFocusable(false);
        ampmBox.setFocusable(false);

        // Pre-fill the time pickers with the task's existing due time, if it's valid.
        // This handles both "HH:mm" and "hh:mm AM/PM" formats since older saved
        // data might still be in 24hr format.
        if (sel.dueTime != null && sel.dueTime.contains(":")) {
            try {
                String s = sel.dueTime.trim();
                String period = null;
                if (s.toUpperCase().endsWith("AM") || s.toUpperCase().endsWith("PM")) {
                    String[] tok = s.split("\\s+");
                    period = tok[tok.length - 1].toUpperCase();
                    s = tok[0];
                }
                String[] hm = s.split(":");
                int hh = Integer.parseInt(hm[0].trim());
                int mm = Integer.parseInt(hm[1].trim());
                if (period == null) {
                    // Time was in 24hr format, so work out AM/PM and the 12hr hour ourselves.
                    period = hh >= 12 ? "PM" : "AM";
                    int h12 = hh % 12;
                    if (h12 == 0) h12 = 12;
                    hourBox.setSelectedItem(String.format("%02d", h12));
                } else {
                    // Time already had AM/PM attached, so just normalize "00" to "12".
                    int display = hh == 0 ? 12 : hh;
                    hourBox.setSelectedItem(String.format("%02d", display));
                }
                String mmStr = String.format("%02d", mm);
                boolean found = false;
                for (int i = 0; i < minuteBox.getItemCount(); i++) {
                    if (minuteBox.getItemAt(i).equals(mmStr)) { found = true; break; }
                }
                // If the saved minute value isn't one of our preset options (00/15/30/45),
                // add it as an extra option so we don't silently change the user's data.
                if (!found) minuteBox.addItem(mmStr);
                minuteBox.setSelectedItem(mmStr);
                ampmBox.setSelectedItem(period);
            } catch (Exception ex) {
                // If parsing fails for any reason, just leave the pickers at their defaults.
            }
        }

        JPanel datePanel = new JPanel();
        datePanel.add(yearBox); datePanel.add(new JLabel("-"));
        datePanel.add(monthBox); datePanel.add(new JLabel("-"));
        datePanel.add(dayBox);

        JPanel timePanel = new JPanel();
        timePanel.add(hourBox); timePanel.add(new JLabel(":"));
        timePanel.add(minuteBox); timePanel.add(ampmBox);

        Object[] fields = {
            "Set status:", statusBox,
            "Change due date:", datePanel,
            "Change due time:", timePanel
        };

        int res = JOptionPane.showConfirmDialog(frame, fields, "Change Status", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return false;

        // Write all the edited values back onto the task.
        sel.status = (Status) statusBox.getSelectedItem();
        sel.dueDate = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
        String dueTime = hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + " " + ampmBox.getSelectedItem();
        sel.dueTime = TimeDisplay.formatTo12Hour(dueTime);
        return true;
    }
}