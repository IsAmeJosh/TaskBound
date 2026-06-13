package ui_and_ux;

import java.awt.Color;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.Scheduler;
import logic.TaskManager;
import logic.TaskSorter;

/* Entry point for the app's UI. Sets up the main window and
   assembles the three tabs: Tasks, Calendar, and Dev Console. */
public class TaskManagerGUI {

    public static void main(String[] args) throws Exception {
        /* Removes the dotted focus rectangle from every button globally,
           including OK and Cancel buttons inside JOptionPane popups that
           cannot be reached directly with setFocusable.
           Must be set before any UI components are created. */
        UIManager.put("Button.focusPainted", false);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        TaskManager tm = new TaskManager();

        /* fakeTodayRef is an array so the Dev Console can reassign it
           inside a lambda. Java lambdas cannot reassign a plain local
           variable, but they can mutate an array element. */
        LocalDate[] fakeTodayRef = {LocalDate.now()};

        Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
        TaskSorter.sort(tm.tasks);

        /* tableModel is shared between the Tasks tab and Dev Console
           so the Dev Console can trigger a table refresh when the
           fake date changes. */
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
        tabs.addTab("Dev Console", DevConsolePanel.build(frame, tm, tableModel, fakeTodayRef));

        /* Re-render the calendar when the user switches to that tab
           so it reflects any task changes made on the Tasks tab. */
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                CalendarPanel.render(tm.tasks);
            }
        });

        frame.add(tabs);
        frame.setVisible(true);
    }
}