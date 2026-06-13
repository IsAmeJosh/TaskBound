package ui_and_ux;

import core.Status;
import core.Task;
import data.FileHandler;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.LMSMockData;
import logic.Scheduler;
import logic.TaskFilter;
import logic.TaskManager;
import logic.TimeDisplay;

// This is the main "Tasks" tab. It's responsible for laying out the filter
// dropdowns, the task table, and the action buttons, then wiring each button
// up to the right logic. The actual popup forms live in TaskDialogs, and the
// actual filtering/time-formatting logic lives in TaskFilter and TimeDisplay.
public class TasksPanel {

    static JComboBox<String> subjectFilterBox;
    static JComboBox<String> statusFilterBox;

    // Builds the whole Tasks tab and returns it ready to drop into a JTabbedPane.
    public static JPanel build(JFrame frame, TaskManager tm, DefaultTableModel tableModel, LocalDate[] fakeTodayRef) {
        JPanel panel = new JPanel(new BorderLayout());

        // ---- Filter bar at the top ----
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Subject:"));
        subjectFilterBox = new JComboBox<>();
        subjectFilterBox.setFocusable(false);
        subjectFilterBox.setPreferredSize(new Dimension(150, 26));
        filterPanel.add(subjectFilterBox);
        filterPanel.add(new JLabel("Due:"));
        statusFilterBox = new JComboBox<>(new String[]{"All", "Overdue", "Due Today", "Upcoming"});
        statusFilterBox.setFocusable(false);
        statusFilterBox.setPreferredSize(new Dimension(120, 26));
        filterPanel.add(statusFilterBox);
        panel.add(filterPanel, BorderLayout.NORTH);

        // Re-filter the table whenever either dropdown changes.
        subjectFilterBox.addActionListener(e -> refresh(tm, tableModel, fakeTodayRef[0]));
        statusFilterBox.addActionListener(e -> refresh(tm, tableModel, fakeTodayRef[0]));

        // ---- The task table itself ----
        // We override prepareRenderer so each row gets a background color
        // based on its status: red-ish for missed, green for complete,
        // yellow for everything else (incomplete/pending).
        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                Object statusVal = getValueAt(row, 4);
                if (isCellSelected(row, col)) {
                    c.setBackground(new Color(100, 149, 237));
                } else if ("MISSED".equals(String.valueOf(statusVal))) {
                    c.setBackground(new Color(255, 182, 182));
                } else if ("COMPLETE".equals(String.valueOf(statusVal))) {
                    c.setBackground(new Color(182, 255, 182));
                } else {
                    c.setBackground(new Color(255, 255, 182));
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        };

        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setFocusable(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setEnabled(false);

        // Make sure every task's due time is in a consistent format before we
        // populate the filter dropdown and the table for the first time.
        TimeDisplay.normalizeAllTimes(tm.tasks);
        TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
        refresh(tm, tableModel, fakeTodayRef[0]);

        // Wrap the table in some padding so it doesn't touch the edges of the window.
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableWrapper, BorderLayout.CENTER);

        // ---- Button bar at the bottom ----
        JPanel buttonPanel = new JPanel();
        JButton syncBtn = new JButton("Sync LMS");
        JButton addBtn = new JButton("Add Task");
        JButton deleteBtn = new JButton("Delete Task");
        JButton statusBtn = new JButton("Change Status");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");

        syncBtn.setFocusable(false);
        addBtn.setFocusable(false);
        deleteBtn.setFocusable(false);
        statusBtn.setFocusable(false);
        saveBtn.setFocusable(false);
        loadBtn.setFocusable(false);

        buttonPanel.add(syncBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(statusBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(loadBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // ---- Sync LMS: replace all tasks with fresh mock data from the LMS ----
        syncBtn.addActionListener(e -> {
            tm.tasks = LMSMockData.getFakeTasks();
            TimeDisplay.normalizeAllTimes(tm.tasks);
            Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
            Scheduler.sortByDueDate(tm.tasks);
            TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
            refresh(tm, tableModel, fakeTodayRef[0]);
            JOptionPane.showMessageDialog(frame, "LMS Synced!");
        });

        // ---- Add Task: show the popup and add whatever it returns ----
        addBtn.addActionListener(e -> {
            Task t = TaskDialogs.showAddTaskDialog(frame);
            if (t == null) return; // user cancelled or left a field empty

            tm.addTask(t);
            Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
            Scheduler.sortByDueDate(tm.tasks);
            TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
            refresh(tm, tableModel, fakeTodayRef[0]);
        });

        // ---- Delete Task: remove every selected row ----
        // We match tasks by title+subject+date instead of row index, because
        // the table can be sorted/filtered differently than the underlying
        // task list, so row index alone isn't reliable.
        deleteBtn.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {
                JOptionPane.showMessageDialog(frame, "Select a task to delete.");
                return;
            }
            ArrayList<Task> toDelete = new ArrayList<>();
            for (int i : rows) {
                String rowTitle   = String.valueOf(tableModel.getValueAt(i, 0));
                String rowSubject = String.valueOf(tableModel.getValueAt(i, 1));
                String rowDate    = String.valueOf(tableModel.getValueAt(i, 2));
                for (Task t : tm.tasks) {
                    if (t.title.equals(rowTitle) && t.subject.equals(rowSubject) && t.dueDate.equals(rowDate)) {
                        toDelete.add(t);
                        break;
                    }
                }
            }
            tm.tasks.removeAll(toDelete);
            TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
            refresh(tm, tableModel, fakeTodayRef[0]);
        });

        // ---- Change Status: find the selected task, then let TaskDialogs edit it ----
        statusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a task first.");
                return;
            }
            // Same title+subject+date matching trick as Delete, for the same reason.
            String rowTitle   = String.valueOf(tableModel.getValueAt(row, 0));
            String rowSubject = String.valueOf(tableModel.getValueAt(row, 1));
            String rowDate    = String.valueOf(tableModel.getValueAt(row, 2));
            Task sel = null;
            for (Task t : tm.tasks) {
                if (t.title.equals(rowTitle) && t.subject.equals(rowSubject) && t.dueDate.equals(rowDate)) {
                    sel = t;
                    break;
                }
            }
            if (sel == null) {
                JOptionPane.showMessageDialog(frame, "Could not find selected task.");
                return;
            }

            boolean confirmed = TaskDialogs.showEditTaskDialog(frame, sel);
            if (confirmed) {
                Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                Scheduler.sortByDueDate(tm.tasks);
                // Clear the selection first so we don't end up highlighting
                // the wrong row after the list gets re-sorted.
                table.clearSelection();
                refresh(tm, tableModel, fakeTodayRef[0]);
            }
        });

        // ---- Save: let the user pick where to save, then write tasks there ----
        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Tasks");
            chooser.setSelectedFile(new File("tasks.csv"));

            int result = chooser.showSaveDialog(frame);
            if (result != JFileChooser.APPROVE_OPTION) return; // user cancelled

            File file = chooser.getSelectedFile();
            // If the user didn't type an extension, default to .csv so the
            // file is easy to recognize and reload later.
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try {
                FileHandler.saveTasks(tm.tasks, file);
                JOptionPane.showMessageDialog(frame, "Saved!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error saving.");
            }
        });

        // ---- Load: let the user pick a previously-saved CSV and load it in ----
        // This is the only way saved tasks get back into the app, since we
        // no longer auto-load on startup. Any unsaved changes in memory are
        // discarded when this is clicked.
        loadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Load Tasks");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));

            int result = chooser.showOpenDialog(frame);
            if (result != JFileChooser.APPROVE_OPTION) return; // user cancelled

            File file = chooser.getSelectedFile();
            try {
                tm.tasks = FileHandler.loadTasks(file);
                tm.tasks.removeIf(t -> t.title == null || t.title.trim().isEmpty());
                TimeDisplay.normalizeAllTimes(tm.tasks);
                Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                Scheduler.sortByDueDate(tm.tasks);
                TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
                refresh(tm, tableModel, fakeTodayRef[0]);
                CalendarPanel.render(tm.tasks);
                JOptionPane.showMessageDialog(frame, "Loaded!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error loading that file.");
            }
        });

        return panel;
    }

    // Rebuilds the table contents based on the current filter selections.
    // Called whenever tasks change, filters change, or the fake date changes.
    public static void refresh(TaskManager tm, DefaultTableModel tableModel, LocalDate fakeToday) {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        ArrayList<Task> filtered = TaskFilter.getFilteredTasks(tm.tasks, subjectFilterBox, statusFilterBox, fakeToday);
        for (Task t : filtered) {
            // For completed or missed tasks, the countdown no longer matters,
            // so leave the "Time Left" column blank instead of showing a
            // stale "X days left"/"Overdue by..." value.
            String timeLeft = (t.status == Status.COMPLETE || t.status == Status.MISSED)
                ? ""
                : TimeDisplay.getTimeLeftDisplay(t.dueDate, t.dueTime, fakeToday);

            tableModel.addRow(new Object[]{
                t.title,
                t.subject,
                t.dueDate,
                TimeDisplay.formatTo12Hour(t.dueTime),
                t.status,
                timeLeft
            });
        }
    }
}