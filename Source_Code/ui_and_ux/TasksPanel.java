package ui_and_ux;

import core.Status;
import core.Task;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.SubjectFilter;
import logic.TaskFilter;
import logic.TaskManager;
import logic.TimeConverter;
import logic.TimeDisplay;

/* The Tasks tab. Responsible only for building the layout:
   the filter bar, the task table, and the button bar.
   All button logic lives in TaskButtons, and all popup forms
   live in TaskDialogs. */
public class TasksPanel {

    static JComboBox<String> subjectFilterBox;
    static JComboBox<String> statusFilterBox;

    /* Builds the Tasks tab and returns it ready to add to the JTabbedPane. */
    public static JPanel build(JFrame frame, TaskManager tm, DefaultTableModel tableModel, LocalDate[] fakeTodayRef) {
        JPanel panel = new JPanel(new BorderLayout());

        /* Filter bar at the top of the tab. */
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

        /* Re-filter the table whenever either dropdown changes. */
        subjectFilterBox.addActionListener(e -> refresh(tm, tableModel, fakeTodayRef[0]));
        statusFilterBox.addActionListener(e -> refresh(tm, tableModel, fakeTodayRef[0]));

        /* The task table. Each row gets a background color based on
           its status: blue when selected, red for missed, green for
           complete, yellow for incomplete. */
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

        /* Normalize all times and populate the filter and table
           before the tab is shown for the first time. */
        TimeConverter.normalizeAllTimes(tm.tasks);
        SubjectFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
        refresh(tm, tableModel, fakeTodayRef[0]);

        /* Wrap the table with padding so it does not touch the window edges. */
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableWrapper, BorderLayout.CENTER);

        /* Button bar at the bottom. TaskButtons wires all the actions. */
        JPanel buttonPanel = new JPanel();
        panel.add(buttonPanel, BorderLayout.SOUTH);
        TaskButtons.wire(frame, tm, tableModel, table, buttonPanel, fakeTodayRef);

        return panel;
    }

    /* Rebuilds the table contents based on the current filter selections.
       Called whenever tasks change, filters change, or the fake date changes. */
    public static void refresh(TaskManager tm, DefaultTableModel tableModel, LocalDate fakeToday) {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        ArrayList<Task> filtered = TaskFilter.getFilteredTasks(tm.tasks, subjectFilterBox, statusFilterBox, fakeToday);
        for (Task t : filtered) {
            /* Leave Time Left blank for complete and missed tasks since
               the countdown is no longer meaningful for them. */
            String timeLeft = (t.status == Status.COMPLETE || t.status == Status.MISSED)
                ? ""
                : TimeDisplay.getTimeLeftDisplay(t.dueDate, t.dueTime, fakeToday);

            tableModel.addRow(new Object[]{
                t.title,
                t.subject,
                t.dueDate,
                TimeConverter.formatTo12Hour(t.dueTime),
                t.status,
                timeLeft
            });
        }
    }
}