package ui_and_ux;

import core.Status;
import core.Task;
import java.util.ArrayList;
import javax.swing.*;
import logic.TimeConverter;

/* Home to all popup dialogs used to create or edit tasks.
   Pulling these out of TasksPanel keeps that file focused on
   layout, while this file focuses purely on building forms,
   showing them, and handing back what the user entered. */
public class TaskDialogs {

    /* Shared options used by the date and time pickers in both dialogs. */
    private static final String[] YEARS = {
        "2024","2025","2026","2027","2028","2029","2030",
        "2031","2032","2033","2034","2035"
    };
    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };
    private static final String[] HOURS_12 = {
        "12","01","02","03","04","05","06","07","08","09","10","11"
    };
    private static final String[] AMPM = {"AM","PM"};

    /* Generates minute options 00 through 59. */
    private static String[] buildMinutes() {
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) minutes[i] = String.format("%02d", i);
        return minutes;
    }

    /* Builds the list of day numbers 01 through 31 for the day dropdown. */
    private static String[] buildDays() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
        return days;
    }

    /* Converts a month name like "June" to its two-digit number "06"
       for storage in the YYYY-MM-DD format. */
    private static String monthNameToNumber(String monthName) {
        for (int i = 0; i < MONTHS.length; i++) {
            if (MONTHS[i].equals(monthName)) {
                return String.format("%02d", i + 1);
            }
        }
        return "01";
    }

    /* Converts a two-digit month number like "06" back to its name
       "June" so the picker can be pre-filled correctly in the edit dialog. */
    private static String monthNumberToName(String monthNumber) {
        try {
            int idx = Integer.parseInt(monthNumber.trim()) - 1;
            if (idx >= 0 && idx < MONTHS.length) return MONTHS[idx];
        } catch (Exception e) {
            /* Fall through to default. */
        }
        return MONTHS[0];
    }

    /* Shows the Add Task popup. Returns a fully filled Task if the user
       confirms with valid input, or null if they cancel or leave a
       required field empty. An error message is shown in the empty case. */
    public static Task showAddTaskDialog(JFrame frame) {
        JTextField titleField = new JTextField();
        JTextField subjectField = new JTextField();

        JComboBox<String> yearBox = new JComboBox<>(YEARS);
        JComboBox<String> monthBox = new JComboBox<>(MONTHS);
        JComboBox<String> dayBox = new JComboBox<>(buildDays());
        JComboBox<String> hourBox = new JComboBox<>(HOURS_12);
        JComboBox<String> minuteBox = new JComboBox<>(buildMinutes());
        JComboBox<String> ampmBox = new JComboBox<>(AMPM);

        /* Default the month and year pickers to the current date. */
        monthBox.setSelectedIndex(java.time.LocalDate.now().getMonthValue() - 1);
        yearBox.setSelectedItem(String.valueOf(java.time.LocalDate.now().getYear()));

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

        /* New tasks always start as INCOMPLETE. */
        Task t = new Task();
        t.title = title;
        t.subject = subject;
        t.dueDate = yearBox.getSelectedItem() + "-"
            + monthNameToNumber((String) monthBox.getSelectedItem()) + "-"
            + dayBox.getSelectedItem();
        String dueTime = hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + " " + ampmBox.getSelectedItem();
        t.dueTime = TimeConverter.formatTo12Hour(dueTime);
        t.status = Status.INCOMPLETE;
        return t;
    }

    /* Shows the Change Status popup pre-filled with the selected task's
       current status, due date, and due time. If the user confirms, the
       task is updated in place and this returns true. Returns false if
       the user cancels, leaving the task untouched. */
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

        /* Pre-fill the date pickers with the task's existing due date. */
        if (sel.dueDate != null && sel.dueDate.contains("-")) {
            String[] p = sel.dueDate.split("-");
            if (p.length == 3) {
                yearBox.setSelectedItem(p[0]);
                monthBox.setSelectedItem(monthNumberToName(p[1]));
                dayBox.setSelectedItem(p[2]);
            }
        }

        JComboBox<String> hourBox = new JComboBox<>(HOURS_12);
        JComboBox<String> minuteBox = new JComboBox<>(buildMinutes());
        JComboBox<String> ampmBox = new JComboBox<>(AMPM);
        hourBox.setFocusable(false);
        minuteBox.setFocusable(false);
        ampmBox.setFocusable(false);

        /* Pre-fill the time pickers with the task's existing due time.
           Handles both HH:mm and hh:mm AM/PM since older saved data
           might still be in 24hr format. */
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
                    /* Time was in 24hr format, work out AM/PM and the 12hr hour. */
                    period = hh >= 12 ? "PM" : "AM";
                    int h12 = hh % 12;
                    if (h12 == 0) h12 = 12;
                    hourBox.setSelectedItem(String.format("%02d", h12));
                } else {
                    /* Time already had AM/PM, just normalize 00 to 12. */
                    int display = hh == 0 ? 12 : hh;
                    hourBox.setSelectedItem(String.format("%02d", display));
                }
                String mmStr = String.format("%02d", mm);
                minuteBox.setSelectedItem(mmStr);
                ampmBox.setSelectedItem(period);
            } catch (Exception ex) {
                /* Leave pickers at defaults if parsing fails. */
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

        /* Write all edited values back onto the task. */
        sel.status = (Status) statusBox.getSelectedItem();
        sel.dueDate = yearBox.getSelectedItem() + "-"
            + monthNameToNumber((String) monthBox.getSelectedItem()) + "-"
            + dayBox.getSelectedItem();
        String dueTime = hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + " " + ampmBox.getSelectedItem();
        sel.dueTime = TimeConverter.formatTo12Hour(dueTime);
        return true;
    }

    /* Shows a congratulations popup when a task is marked COMPLETE. */
    public static void showCompletedPopup(JFrame frame, Task t) {
        JOptionPane.showMessageDialog(
            frame,
            "Great job finishing \"" + t.title + "\"! Keep it up!",
            "Task Completed",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /* Shows an encouragement popup when a task is manually marked MISSED. */
    public static void showSingleMissedPopup(JFrame frame, Task t) {
        JOptionPane.showMessageDialog(
            frame,
            "\"" + t.title + "\" was marked as missed. Don't worry, you can still catch up!",
            "Task Missed",
            JOptionPane.WARNING_MESSAGE
        );
    }

    /* Shows an encouragement popup for tasks that just became MISSED
       automatically after a sync, load, or Dev Console date change.
       Wording adjusts for singular vs plural. */
    public static void showNewlyMissedPopup(JFrame frame, ArrayList<Task> newlyMissed) {
        if (newlyMissed == null || newlyMissed.isEmpty()) return;
        String message;
        if (newlyMissed.size() == 1) {
            message = "You missed 1 task: \"" + newlyMissed.get(0).title + "\". Don't worry, keep going!";
        } else {
            message = "You missed " + newlyMissed.size() + " tasks. Don't worry, keep going!";
        }
        JOptionPane.showMessageDialog(frame, message, "Missed Tasks", JOptionPane.WARNING_MESSAGE);
    }
}