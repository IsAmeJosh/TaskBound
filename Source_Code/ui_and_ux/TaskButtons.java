package ui_and_ux;

import core.Status;
import core.Task;
import data.FileHandler;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.LMSMockData;
import logic.Scheduler;
import logic.SubjectFilter;
import logic.TaskManager;
import logic.TaskSorter;
import logic.TimeConverter;

/* Wires up all the action buttons on the Tasks tab.
   Kept separate from TasksPanel so the layout file only deals
   with building the UI, while this file deals with what
   each button actually does when clicked. */
public class TaskButtons {

    /* Attaches action listeners to all six buttons and adds them
       to the given button panel. The table and tableModel are needed
       so button actions can read the selected row and refresh the display. */
    public static void wire(
            JFrame frame,
            TaskManager tm,
            DefaultTableModel tableModel,
            JTable table,
            JPanel buttonPanel,
            LocalDate[] fakeTodayRef) {

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

        /* Sync LMS: replace all tasks with fresh mock data. */
        syncBtn.addActionListener(e -> {
            tm.tasks = LMSMockData.getFakeTasks();
            TimeConverter.normalizeAllTimes(tm.tasks);
            ArrayList<Task> newlyMissed = Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
            TaskSorter.sort(tm.tasks);
            SubjectFilter.refreshSubjectFilterOptions(tm.tasks, TasksPanel.subjectFilterBox);
            TasksPanel.refresh(tm, tableModel, fakeTodayRef[0]);

            StringBuilder sb = new StringBuilder();
            sb.append("Date: ").append(fakeTodayRef[0]).append("\n");
            sb.append("LMS Synced!\n");
            sb.append("Total tasks: ").append(tm.tasks.size()).append("\n");
            for (Task t : newlyMissed) {
                sb.append("CHANGED: ").append(t.title).append(" -> MISSED\n");
            }
            sb.append("Done.\n");
            DevConsolePanel.setLog(sb.toString());

            JOptionPane.showMessageDialog(frame, "LMS Synced!");
            TaskDialogs.showNewlyMissedPopup(frame, newlyMissed);
        });

        /* Add Task: show the add dialog and insert the returned task. */
        addBtn.addActionListener(e -> {
            Task t = TaskDialogs.showAddTaskDialog(frame);
            if (t == null) return;
            tm.addTask(t);
            Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
            TaskSorter.sort(tm.tasks);
            SubjectFilter.refreshSubjectFilterOptions(tm.tasks, TasksPanel.subjectFilterBox);
            TasksPanel.refresh(tm, tableModel, fakeTodayRef[0]);

            StringBuilder sb = new StringBuilder();
            sb.append("Date: ").append(fakeTodayRef[0]).append("\n");
            sb.append("ADDED: ").append(t.title).append("\n");
            sb.append("Total tasks: ").append(tm.tasks.size()).append("\n");
            sb.append("Done.\n");
            DevConsolePanel.setLog(sb.toString());
        });

        /* Delete Task: remove every selected row, matched by
           title+subject+date to avoid index mismatch after sorting. */
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
            SubjectFilter.refreshSubjectFilterOptions(tm.tasks, TasksPanel.subjectFilterBox);
            TasksPanel.refresh(tm, tableModel, fakeTodayRef[0]);

            StringBuilder sb = new StringBuilder();
            sb.append("Date: ").append(fakeTodayRef[0]).append("\n");
            for (Task t : toDelete) {
                sb.append("REMOVED: ").append(t.title).append("\n");
            }
            sb.append("Total tasks: ").append(tm.tasks.size()).append("\n");
            sb.append("Done.\n");
            DevConsolePanel.setLog(sb.toString());
        });

        /* Change Status: find the selected task by title+subject+date,
           show the edit dialog, then re-sort and refresh. */
        statusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a task first.");
                return;
            }
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

            Status statusBefore = sel.status;
            boolean confirmed = TaskDialogs.showEditTaskDialog(frame, sel);
            if (confirmed) {
                ArrayList<Task> newlyMissed = Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                TaskSorter.sort(tm.tasks);
                table.clearSelection();
                TasksPanel.refresh(tm, tableModel, fakeTodayRef[0]);

                StringBuilder sb = new StringBuilder();
                sb.append("Date: ").append(fakeTodayRef[0]).append("\n");
                sb.append("CHANGED: ").append(sel.title).append(" -> ").append(sel.status).append("\n");
                for (Task t : newlyMissed) {
                    if (t != sel) sb.append("CHANGED: ").append(t.title).append(" -> MISSED\n");
                }
                sb.append("Total tasks: ").append(tm.tasks.size()).append("\n");
                sb.append("Done.\n");
                DevConsolePanel.setLog(sb.toString());

                if (statusBefore != Status.COMPLETE && sel.status == Status.COMPLETE) {
                    TaskDialogs.showCompletedPopup(frame, sel);
                } else if (statusBefore != Status.MISSED && sel.status == Status.MISSED) {
                    TaskDialogs.showSingleMissedPopup(frame, sel);
                }
                newlyMissed.remove(sel);
                if (!newlyMissed.isEmpty()) {
                    TaskDialogs.showNewlyMissedPopup(frame, newlyMissed);
                }
            }
        });

        /* Save: open a file chooser and write tasks to the chosen file. */
        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Tasks");
            chooser.setSelectedFile(new File("tasks.csv"));
            int result = chooser.showSaveDialog(frame);
            if (result != JFileChooser.APPROVE_OPTION) return;
            File file = chooser.getSelectedFile();
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

        /* Load: open a file chooser and replace the task list with
           whatever is in the chosen CSV file. */
        loadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Load Tasks");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
            int result = chooser.showOpenDialog(frame);
            if (result != JFileChooser.APPROVE_OPTION) return;
            File file = chooser.getSelectedFile();
            try {
                tm.tasks = FileHandler.loadTasks(file);
                tm.tasks.removeIf(t -> t.title == null || t.title.trim().isEmpty());
                TimeConverter.normalizeAllTimes(tm.tasks);
                ArrayList<Task> newlyMissed = Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                TaskSorter.sort(tm.tasks);
                SubjectFilter.refreshSubjectFilterOptions(tm.tasks, TasksPanel.subjectFilterBox);
                TasksPanel.refresh(tm, tableModel, fakeTodayRef[0]);
                CalendarPanel.render(tm.tasks);

                StringBuilder sb = new StringBuilder();
                sb.append("Date: ").append(fakeTodayRef[0]).append("\n");
                sb.append("Loaded: ").append(file.getName()).append("\n");
                sb.append("Total tasks: ").append(tm.tasks.size()).append("\n");
                for (Task t : newlyMissed) {
                    sb.append("CHANGED: ").append(t.title).append(" -> MISSED\n");
                }
                sb.append("Done.\n");
                DevConsolePanel.setLog(sb.toString());

                JOptionPane.showMessageDialog(frame, "Loaded!");
                TaskDialogs.showNewlyMissedPopup(frame, newlyMissed);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error loading that file.");
            }
        });
    }
}