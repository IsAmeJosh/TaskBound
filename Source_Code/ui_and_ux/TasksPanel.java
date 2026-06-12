package ui_and_ux;

import core.Task;
import core.Status;
import data.FileHandler;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.LMSMockData;
import logic.Scheduler;
import logic.TaskFilter;
import logic.TaskManager;
import logic.TimeDisplay;

public class TasksPanel {

    static JComboBox<String> subjectFilterBox;
    static JComboBox<String> statusFilterBox;

    public static JPanel build(JFrame frame, TaskManager tm, DefaultTableModel tableModel, LocalDate[] fakeTodayRef) {
        JPanel panel = new JPanel(new BorderLayout());

        // Filter panel
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

        subjectFilterBox.addActionListener(e -> refresh(tm, tableModel, fakeTodayRef[0]));
        statusFilterBox.addActionListener(e -> refresh(tm, tableModel, fakeTodayRef[0]));

        // Table with padding from edges
        JTable table = new JTable(tableModel) {
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isCellSelected(row, col)) {
                    c.setBackground(new Color(100, 149, 237));
                } else {
                    Object statusVal = getValueAt(row, 4);
                    if ("MISSED".equals(String.valueOf(statusVal))) {
                        c.setBackground(new Color(255, 182, 182));
                    } else if ("COMPLETE".equals(String.valueOf(statusVal))) {
                        c.setBackground(new Color(182, 255, 182));
                    } else {
                        c.setBackground(new Color(255, 255, 182));
                    }
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        };

        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setEnabled(false);
        table.setFocusable(false);

        TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
        refresh(tm, tableModel, fakeTodayRef[0]);

        JScrollPane scrollPane = new JScrollPane(table);
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableWrapper, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton syncBtn = new JButton("Sync LMS");
        JButton addBtn = new JButton("Add Task");
        JButton deleteBtn = new JButton("Delete Task");
        JButton statusBtn = new JButton("Change Status");
        JButton saveBtn = new JButton("Save");

        syncBtn.setFocusable(false);
        addBtn.setFocusable(false);
        deleteBtn.setFocusable(false);
        statusBtn.setFocusable(false);
        saveBtn.setFocusable(false);

        buttonPanel.add(syncBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(statusBtn);
        buttonPanel.add(saveBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        syncBtn.addActionListener(e -> {
            tm.tasks = LMSMockData.getFakeTasks();
            Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
            Scheduler.sortByDueDate(tm.tasks);
            TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
            refresh(tm, tableModel, fakeTodayRef[0]);
            JOptionPane.showMessageDialog(frame, "LMS Synced!");
        });

        addBtn.addActionListener(e -> {
            JTextField titleField = new JTextField();
            JTextField subjectField = new JTextField();

            String[] years = {"2026", "2027", "2028"};
            String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
            String[] days = new String[31];
            for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
            String[] hours = new String[24];
            for (int i = 0; i < 24; i++) hours[i] = String.format("%02d", i);
            String[] minutes = {"00", "15", "30", "45"};

            JComboBox<String> yearBox = new JComboBox<>(years);
            JComboBox<String> monthBox = new JComboBox<>(months);
            JComboBox<String> dayBox = new JComboBox<>(days);
            JComboBox<String> hourBox = new JComboBox<>(hours);
            JComboBox<String> minuteBox = new JComboBox<>(minutes);

            JPanel datePanel = new JPanel();
            datePanel.add(yearBox);
            datePanel.add(new JLabel("-"));
            datePanel.add(monthBox);
            datePanel.add(new JLabel("-"));
            datePanel.add(dayBox);

            JPanel timePanel = new JPanel();
            timePanel.add(hourBox);
            timePanel.add(new JLabel(":"));
            timePanel.add(minuteBox);

            Object[] fields = {
                "Task:", titleField,
                "Subject:", subjectField,
                "Due Date:", datePanel,
                "Due Time:", timePanel
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Add Task", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String title = titleField.getText().trim();
                String subject = subjectField.getText().trim();

                if (title.isEmpty() || subject.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Task and Subject cannot be empty.");
                    return;
                }

                Task t = new Task();
                t.title = title;
                t.subject = subject;
                t.dueDate = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
                t.dueTime = hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem();
                t.status = Status.INCOMPLETE;
                tm.addTask(t);
                Scheduler.sortByDueDate(tm.tasks);
                TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
                refresh(tm, tableModel, fakeTodayRef[0]);
            }
        });

        deleteBtn.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if (rows.length > 0) {
                ArrayList<Task> filtered = TaskFilter.getFilteredTasks(tm.tasks, subjectFilterBox, statusFilterBox, fakeTodayRef[0]);
                ArrayList<Task> toDelete = new ArrayList<>();
                for (int i = rows.length - 1; i >= 0; i--) {
                    toDelete.add(filtered.get(rows[i]));
                }
                tm.tasks.removeAll(toDelete);
                TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
                refresh(tm, tableModel, fakeTodayRef[0]);
            } else {
                JOptionPane.showMessageDialog(frame, "Select a task to delete.");
            }
        });

        statusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a task first.");
                return;
            }
            ArrayList<Task> filtered = TaskFilter.getFilteredTasks(tm.tasks, subjectFilterBox, statusFilterBox, fakeTodayRef[0]);
            Task selected = filtered.get(row);
            Status[] options = {Status.INCOMPLETE, Status.COMPLETE, Status.MISSED};
            Status choice = (Status) JOptionPane.showInputDialog(frame, "Set status:", "Change Status",
                    JOptionPane.QUESTION_MESSAGE, null, options, selected.status);
            if (choice != null) {
                selected.status = choice;
                refresh(tm, tableModel, fakeTodayRef[0]);
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

        return panel;
    }

    public static void refresh(TaskManager tm, DefaultTableModel tableModel, LocalDate fakeToday) {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (Task t : TaskFilter.getFilteredTasks(tm.tasks, subjectFilterBox, statusFilterBox, fakeToday)) {
            tableModel.addRow(new Object[]{
                t.title, t.subject, t.dueDate, t.dueTime, t.status,
                TimeDisplay.getTimeLeftDisplay(t.dueDate, t.dueTime, fakeToday)
            });
        }
    }
}