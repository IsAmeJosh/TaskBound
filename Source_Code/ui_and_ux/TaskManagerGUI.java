package ui_and_ux;

import core.Status;
import core.Task;
import data.FileHandler;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.LMSMockData;
import logic.TaskManager;

public class TaskManagerGUI {

    static TaskManager tm = new TaskManager();
    static DefaultTableModel tableModel;

    static JComboBox<String> subjectFilterBox;
    static JComboBox<String> statusFilterBox;

    public static void main(String[] args) throws Exception {
        try {
            tm.tasks = FileHandler.loadTasks();
            tm.tasks.removeIf(t -> t.title == null || t.title.trim().isEmpty());
        } catch (Exception e) {
            // No saved file yet, start empty
        }

        JFrame frame = new JFrame("TaskBound");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 480);
        frame.setLayout(new BorderLayout());

        String[] columns = {"Title", "Subject", "Due Date", "Status", "Time Left"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        // ---- Top filter panel ----
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Subject:"));
        subjectFilterBox = new JComboBox<>();
        filterPanel.add(subjectFilterBox);

        filterPanel.add(new JLabel("Due:"));
        statusFilterBox = new JComboBox<>(new String[]{"All", "Overdue", "Due Today", "Upcoming"});
        filterPanel.add(statusFilterBox);

        frame.add(filterPanel, BorderLayout.NORTH);

        subjectFilterBox.addActionListener(e -> refreshTable());
        statusFilterBox.addActionListener(e -> refreshTable());

        refreshSubjectFilterOptions();
        refreshTable();

        // ---- Bottom button panel ----
        JPanel buttonPanel = new JPanel();
        JButton syncBtn = new JButton("Sync LMS");
        JButton addBtn = new JButton("Add Task");
        JButton deleteBtn = new JButton("Delete Task");
        JButton saveBtn = new JButton("Save");
        JButton statusBtn = new JButton("Change Status");

        buttonPanel.add(syncBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(statusBtn);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        syncBtn.addActionListener(e -> {
            tm.tasks = LMSMockData.getFakeTasks();
            refreshSubjectFilterOptions();
            refreshTable();
            JOptionPane.showMessageDialog(frame, "LMS Synced!");
        });

        addBtn.addActionListener(e -> {
            JTextField titleField = new JTextField();
            JTextField subjectField = new JTextField();

            String[] years = {"2026", "2027", "2028"};
            String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
            String[] days = new String[31];
            for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);

            JComboBox<String> yearBox = new JComboBox<>(years);
            JComboBox<String> monthBox = new JComboBox<>(months);
            JComboBox<String> dayBox = new JComboBox<>(days);

            JPanel datePanel = new JPanel();
            datePanel.add(yearBox);
            datePanel.add(new JLabel("-"));
            datePanel.add(monthBox);
            datePanel.add(new JLabel("-"));
            datePanel.add(dayBox);

            Object[] fields = {
                "Title:", titleField,
                "Subject:", subjectField,
                "Due Date:", datePanel
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Add Task", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String title = titleField.getText().trim();
                String subject = subjectField.getText().trim();

                if (title.isEmpty() || subject.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Title and Subject cannot be empty.");
                    return;
                }

                Task t = new Task();
                t.title = title;
                t.subject = subject;
                t.dueDate = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
                t.dueTime = "23:59";
                t.status = Status.INCOMPLETE;
                tm.addTask(t);
                refreshSubjectFilterOptions();
                refreshTable();
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Task displayed = getFilteredTasks().get(row);
                tm.tasks.remove(displayed);
                refreshSubjectFilterOptions();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(frame, "Select a task to delete.");
            }
        });

        saveBtn.addActionListener(e -> {
            try {
                FileHandler.saveTasks(tm.tasks);
                JOptionPane.showMessageDialog(frame, "Saved!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error saving.");
            }
        });

        statusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a task first.");
                return;
            }
            Task selected = getFilteredTasks().get(row);
            Status[] options = {Status.INCOMPLETE, Status.COMPLETE, Status.MISSED};
            Status choice = (Status) JOptionPane.showInputDialog(frame, "Set status:", "Change Status",
                    JOptionPane.QUESTION_MESSAGE, null, options, selected.status);
            if (choice != null) {
                selected.status = choice;
                refreshTable();
            }
        });

        frame.setVisible(true);
    }

    /** Rebuilds the subject filter dropdown based on current tasks, preserving selection if possible. */
    static void refreshSubjectFilterOptions() {
        String previous = (String) subjectFilterBox.getSelectedItem();
        subjectFilterBox.removeAllItems();
        subjectFilterBox.addItem("All");

        java.util.LinkedHashSet<String> subjects = new java.util.LinkedHashSet<>();
        for (Task t : tm.tasks) {
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

    /** Returns the list of tasks matching the current subject + due-status filters, in order. */
    static java.util.ArrayList<Task> getFilteredTasks() {
        java.util.ArrayList<Task> result = new java.util.ArrayList<>();

        String subjectFilter = subjectFilterBox != null ? (String) subjectFilterBox.getSelectedItem() : "All";
        String dueFilter = statusFilterBox != null ? (String) statusFilterBox.getSelectedItem() : "All";

        for (Task t : tm.tasks) {
            if (subjectFilter != null && !subjectFilter.equals("All")) {
                if (t.subject == null || !t.subject.equals(subjectFilter)) {
                    continue;
                }
            }

            if (dueFilter != null && !dueFilter.equals("All")) {
                String category = getDueCategory(t.dueDate, t.dueTime);
                if (!dueFilter.equals(category)) {
                    continue;
                }
            }

            result.add(t);
        }
        return result;
    }

    /**
     * Categorizes a task's due date/time as "Overdue", "Due Today", or "Upcoming"
     * relative to the current date. Returns "Unknown" if dueDate can't be parsed.
     */
    static String getDueCategory(String dueDate, String dueTime) {
        LocalDate due = parseDate(dueDate);
        if (due == null) return "Unknown";

        LocalDate today = LocalDate.now();
        if (due.isBefore(today)) {
            return "Overdue";
        } else if (due.isEqual(today)) {
            return "Due Today";
        } else {
            return "Upcoming";
        }
    }

    /**
     * Builds a human-readable "time left" string:
     * - "Overdue by X day(s)" if in the past
     * - "Xh Ym left" if due today (counts down to dueTime, or end of day if dueTime missing/unparsable)
     * - "X day(s) left" if upcoming
     * - "Unknown" if dueDate can't be parsed
     */
    static String getTimeLeftDisplay(String dueDate, String dueTime) {
        LocalDate due = parseDate(dueDate);
        if (due == null) return "Unknown";

        LocalDate today = LocalDate.now();

        if (due.isBefore(today)) {
            long daysOverdue = ChronoUnit.DAYS.between(due, today);
            return "Overdue by " + daysOverdue + (daysOverdue == 1 ? " day" : " days");
        } else if (due.isEqual(today)) {
            LocalTime cutoffTime = parseTime(dueTime);
            if (cutoffTime == null) cutoffTime = LocalTime.MAX;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoff = LocalDateTime.of(today, cutoffTime);

            long minutesLeft = ChronoUnit.MINUTES.between(now, cutoff);
            if (minutesLeft < 0) minutesLeft = 0;
            long hours = minutesLeft / 60;
            long minutes = minutesLeft % 60;
            return hours + "h " + minutes + "m left";
        } else {
            long daysLeft = ChronoUnit.DAYS.between(today, due);
            return daysLeft + (daysLeft == 1 ? " day left" : " days left");
        }
    }

    /** Parses a "yyyy-M-d" or "yyyy-MM-dd" style date string. Returns null on failure. */
    static LocalDate parseDate(String dueDate) {
        if (dueDate == null) return null;
        try {
            String[] parts = dueDate.trim().split("-");
            if (parts.length != 3) return null;
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    /** Parses an "HH:mm" style time string. Returns null on failure or if input is null/empty. */
    static LocalTime parseTime(String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) return null;
        try {
            String[] parts = dueTime.trim().split(":");
            if (parts.length != 2) return null;
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return null;
        }
    }

    static void refreshTable() {
        tableModel.setRowCount(0);
        for (Task t : getFilteredTasks()) {
            tableModel.addRow(new Object[]{t.title, t.subject, t.dueDate, t.status, getTimeLeftDisplay(t.dueDate, t.dueTime)});
        }
    }
}