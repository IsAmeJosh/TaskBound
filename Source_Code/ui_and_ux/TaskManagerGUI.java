package ui_and_ux;

import data.FileHandler;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.Scheduler;
import logic.TaskManager;

public class TaskManagerGUI {

    public static void main(String[] args) throws Exception {
        TaskManager tm = new TaskManager();
        LocalDate[] fakeTodayRef = {LocalDate.now()};

        try {
            tm.tasks = FileHandler.loadTasks();
            tm.tasks.removeIf(t -> t.title == null || t.title.trim().isEmpty());
        } catch (Exception e) {
            // No saved file yet, start empty
        }

        Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
        Scheduler.sortByDueDate(tm.tasks);

        String[] columns = {"Task", "Subject", "Due Date", "Due Time", "Status", "Time Left"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JFrame frame = new JFrame("TaskBound");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 580);
        frame.setLayout(new java.awt.BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFocusable(false);
        tabs.addTab("Tasks", TasksPanel.build(frame, tm, tableModel, fakeTodayRef));
        tabs.addTab("Calendar", CalendarPanel.build(tm.tasks));
        tabs.addTab("Dev Console", DevConsolePanel.build(tm, tableModel, fakeTodayRef));

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                CalendarPanel.render(tm.tasks);
            }
        });

        frame.add(tabs);
        frame.setVisible(true);
    }
}