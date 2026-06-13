package ui_and_ux;

import core.Status;
import core.Task;
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

    // Convert any time string to 12hr display format
    static String to12Hour(String time) {
        if (time == null) return "";
        String s = time.trim();
        if (s.isEmpty()) return "";
        String up = s.toUpperCase();
        if (up.endsWith("AM") || up.endsWith("PM")) {
            String[] parts = s.split("\\s+");
            String timePart = parts[0];
            String ampm = parts[parts.length - 1].toUpperCase();
            if (!timePart.contains(":")) return timePart + " " + ampm;
            String[] hm = timePart.split(":");
            try {
                int hh = Integer.parseInt(hm[0].trim());
                int mm = Integer.parseInt(hm[1].trim());
                return String.format("%02d:%02d %s", hh, mm, ampm);
            } catch (Exception e) {
                return timePart + " " + ampm;
            }
        }
        if (s.contains(":")) {
            try {
                String[] p = s.split(":");
                int hh = Integer.parseInt(p[0].trim());
                int mm = Integer.parseInt(p[1].trim().split("\\s+")[0]);
                String period = hh >= 12 ? "PM" : "AM";
                int h12 = hh % 12;
                if (h12 == 0) h12 = 12;
                return String.format("%02d:%02d %s", h12, mm, period);
            } catch (Exception e) {
                return s;
            }
        }
        return s;
    }

    // Normalize all task times to 12hr on load
    static void normalizeAllTimes(TaskManager tm) {
        if (tm == null || tm.tasks == null) return;
        for (Task t : tm.tasks) {
            if (t != null) t.dueTime = to12Hour(t.dueTime);
        }
    }

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

        // Table
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

        normalizeAllTimes(tm);
        TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
        refresh(tm, tableModel, fakeTodayRef[0]);

        // Table with padding from edges
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableWrapper, BorderLayout.CENTER);

        // Button panel
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
            normalizeAllTimes(tm);
            Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
            Scheduler.sortByDueDate(tm.tasks);
            TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
            refresh(tm, tableModel, fakeTodayRef[0]);
            JOptionPane.showMessageDialog(frame, "LMS Synced!");
        });

        addBtn.addActionListener(e -> {
            JTextField titleField = new JTextField();
            JTextField subjectField = new JTextField();

            String[] years = {"2026","2027","2028"};
            String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
            String[] days = new String[31];
            for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);
            String[] hours12 = {"12","01","02","03","04","05","06","07","08","09","10","11"};
            String[] minutes = {"00","15","30","45"};
            String[] ampm = {"AM","PM"};

            JComboBox<String> yearBox = new JComboBox<>(years);
            JComboBox<String> monthBox = new JComboBox<>(months);
            JComboBox<String> dayBox = new JComboBox<>(days);
            JComboBox<String> hourBox = new JComboBox<>(hours12);
            JComboBox<String> minuteBox = new JComboBox<>(minutes);
            JComboBox<String> ampmBox = new JComboBox<>(ampm);

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
            if (res == JOptionPane.OK_OPTION) {
                String title = titleField.getText().trim();
                String subject = subjectField.getText().trim();
                if (title.isEmpty() || subject.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Task and Subject cannot be empty.");
                    return;
                }
                String dueTime = hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + " " + ampmBox.getSelectedItem();
                Task t = new Task();
                t.title = title;
                t.subject = subject;
                t.dueDate = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
                t.dueTime = to12Hour(dueTime);
                t.status = Status.INCOMPLETE;
                tm.addTask(t);
                Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                Scheduler.sortByDueDate(tm.tasks);
                TaskFilter.refreshSubjectFilterOptions(tm.tasks, subjectFilterBox);
                refresh(tm, tableModel, fakeTodayRef[0]);
            }
        });

        deleteBtn.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {
                JOptionPane.showMessageDialog(frame, "Select a task to delete.");
                return;
            }
            // Match by title+subject+date to avoid index mismatch
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

        statusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a task first.");
                return;
            }
            // Match by title+subject+date to avoid index mismatch after sort
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

            Status[] options = {Status.INCOMPLETE, Status.COMPLETE, Status.MISSED};
            JComboBox<Status> statusBox = new JComboBox<>(options);
            statusBox.setSelectedItem(sel.status);

            String[] years = {"2026","2027","2028"};
            String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
            String[] days = new String[31];
            for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);

            JComboBox<String> yearBox = new JComboBox<>(years);
            JComboBox<String> monthBox = new JComboBox<>(months);
            JComboBox<String> dayBox = new JComboBox<>(days);

            if (sel.dueDate != null && sel.dueDate.contains("-")) {
                String[] p = sel.dueDate.split("-");
                if (p.length == 3) {
                    yearBox.setSelectedItem(p[0]);
                    monthBox.setSelectedItem(p[1]);
                    dayBox.setSelectedItem(p[2]);
                }
            }

            String[] hours12 = {"12","01","02","03","04","05","06","07","08","09","10","11"};
            String[] minutes = {"00","15","30","45"};
            String[] ampm = {"AM","PM"};
            JComboBox<String> hourBox = new JComboBox<>(hours12);
            JComboBox<String> minuteBox = new JComboBox<>(minutes);
            JComboBox<String> ampmBox = new JComboBox<>(ampm);

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
                        period = hh >= 12 ? "PM" : "AM";
                        int h12 = hh % 12;
                        if (h12 == 0) h12 = 12;
                        hourBox.setSelectedItem(String.format("%02d", h12));
                    } else {
                        int display = hh == 0 ? 12 : hh;
                        hourBox.setSelectedItem(String.format("%02d", display));
                    }
                    String mmStr = String.format("%02d", mm);
                    boolean found = false;
                    for (int i = 0; i < minuteBox.getItemCount(); i++) {
                        if (minuteBox.getItemAt(i).equals(mmStr)) { found = true; break; }
                    }
                    if (!found) minuteBox.addItem(mmStr);
                    minuteBox.setSelectedItem(mmStr);
                    ampmBox.setSelectedItem(period);
                } catch (Exception ex) {
                    // leave defaults if parse fails
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
            if (res == JOptionPane.OK_OPTION) {
                sel.status = (Status) statusBox.getSelectedItem();
                sel.dueDate = yearBox.getSelectedItem() + "-" + monthBox.getSelectedItem() + "-" + dayBox.getSelectedItem();
                sel.dueTime = to12Hour(hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + " " + ampmBox.getSelectedItem());
                Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                Scheduler.sortByDueDate(tm.tasks);
                // Clear selection before refresh to prevent stale row index
                table.clearSelection();
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
        ArrayList<Task> filtered = TaskFilter.getFilteredTasks(tm.tasks, subjectFilterBox, statusFilterBox, fakeToday);
        for (Task t : filtered) {
            tableModel.addRow(new Object[]{
                t.title,
                t.subject,
                t.dueDate,
                to12Hour(t.dueTime),
                t.status,
                TimeDisplay.getTimeLeftDisplay(t.dueDate, t.dueTime, fakeToday)
            });
        }
    }
}