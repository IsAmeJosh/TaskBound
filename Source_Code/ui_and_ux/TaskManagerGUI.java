package ui_and_ux;

import java.awt.Color;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.Scheduler;
import logic.TaskManager;

// Entry point for the whole app's UI. Sets up the main window, loads any
// saved tasks, and arranges the three tabs: Tasks, Calendar, and Dev Console.
public class TaskManagerGUI {

    public static void main(String[] args) throws Exception {
        // This turns off the dotted focus rectangle on every button in the
        // app, including the OK/Cancel buttons inside JOptionPane popups
        // (Add Task, Change Status, message boxes, etc.) which we can't
        // reach directly with setFocusable since Swing builds those buttons
        // internally. Must be set before any UI components are created.
        UIManager.put("Button.focusPainted", false);
        // The Windows Look and Feel ignores Button.focusPainted for the
        // little focus rectangle drawn around a button's text. This extra
        // property targets that Windows-specific behavior directly.
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        TaskManager tm = new TaskManager();

        // fakeTodayRef holds "today" as far as the app is concerned. It's an
        // array (not a plain LocalDate) so the Dev Console can change it and
        // have every other panel see the update, since Java lambdas can't
        // reassign a captured local variable directly.
        LocalDate[] fakeTodayRef = {LocalDate.now()};

        // The app always starts with an empty task list, even if tasks.csv
        // exists on disk. Users load saved tasks explicitly via the "Load"
        // button on the Tasks tab.

        // Make sure any overdue tasks are flagged as MISSED, and sort the
        // list so the most relevant tasks show up first.
        Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
        Scheduler.sortByDueDate(tm.tasks);

        // This table model is shared between the Tasks tab and the Dev
        // Console, since the Dev Console needs to trigger a table refresh
        // when the fake date changes.
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

        // Re-render the calendar whenever the user switches to that tab, so
        // it reflects any task changes made while on the other tabs.
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                CalendarPanel.render(tm.tasks);
            }
        });

        frame.add(tabs);
        frame.setVisible(true);
    }
}