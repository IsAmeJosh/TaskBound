package ui_and_ux;

import core.Task;
import core.Status;
import data.FileHandler;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.LMSMockData;
import logic.Scheduler;
import logic.TaskManager;

public class TaskManagerGUI {

    static TaskManager tm = new TaskManager();
    static DefaultTableModel tableModel;
    static JTextArea devLog;
    static LocalDate fakeToday = LocalDate.now();
    static JPanel calendarGrid;
    static JLabel monthLabel;
    static YearMonth currentMonth = YearMonth.now();
    static JTextArea calendarTaskDisplay = new JTextArea();

    public static void main(String[] args) throws Exception {
        try {
            tm.tasks = FileHandler.loadTasks();
            tm.tasks.removeIf(t -> t.title == null || t.title.trim().isEmpty());
        } catch (Exception e) {
            // No saved file yet, start empty
        }

        Scheduler.checkAndMarkMissed(tm.tasks, fakeToday);
        Scheduler.sortByDueDate(tm.tasks);

        JFrame frame = new JFrame("TaskBound");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 550);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tasks", buildTasksPanel(frame));
        tabs.addTab("Calendar", buildCalendarPanel());
        tabs.addTab("Dev Console", buildDevConsolePanel());

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                renderCalendar(calendarTaskDisplay);
            }
        });

        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // TASKS TAB
    static JPanel buildTasksPanel(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Title", "Subject", "Due Date", "Due Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

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
                return c;
            }
        };

        refreshTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton syncBtn = new JButton("Sync LMS");
        JButton addBtn = new JButton("Add Task");
        JButton deleteBtn = new JButton("Delete Task");
        JButton completeBtn = new JButton("Mark Complete");
        JButton saveBtn = new JButton("Save (Doesnt matter yet");

        buttonPanel.add(syncBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(completeBtn);
        buttonPanel.add(saveBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        syncBtn.addActionListener(e -> {
            tm.tasks = LMSMockData.getFakeTasks();
            Scheduler.checkAndMarkMissed(tm.tasks, fakeToday);
            Scheduler.sortByDueDate(tm.tasks);
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

            String[] hours = new String[12];
            for (int i = 0; i < 12; i++) hours[i] = String.format("%02d", i + 1);
            String[] minutes = {"00", "15", "30", "45"};
            String[] ampm = {"AM", "PM"};

            JComboBox<String> yearBox = new JComboBox<>(years);
            JComboBox<String> monthBox = new JComboBox<>(months);
            JComboBox<String> dayBox = new JComboBox<>(days);
            JComboBox<String> hourBox = new JComboBox<>(hours);
            JComboBox<String> minuteBox = new JComboBox<>(minutes);
            JComboBox<String> ampmBox = new JComboBox<>(ampm);

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
            timePanel.add(ampmBox);

            Object[] fields = {
                "Title:", titleField,
                "Subject:", subjectField,
                "Due Date:", datePanel,
                "Due Time:", timePanel
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
                t.dueTime = hourBox.getSelectedItem() + ":" + minuteBox.getSelectedItem() + " " + ampmBox.getSelectedItem();
                t.status = Status.INCOMPLETE;
                tm.addTask(t);
                Scheduler.sortByDueDate(tm.tasks);
                refreshTable();
            }
        });

        deleteBtn.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if (rows.length > 0) {
                for (int i = rows.length - 1; i >= 0; i--) {
                    tm.deleteTask(rows[i]);
                }
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(frame, "Select a task to delete.");
            }
        });

        completeBtn.addActionListener(e -> {
            int[] rows = table.getSelectedRows();
            if (rows.length > 0) {
                for (int row : rows) {
                    tm.markComplete(row);
                }
                refreshTable();
                JOptionPane.showMessageDialog(frame, "Good job! Task(s) marked as complete!");
            } else {
                JOptionPane.showMessageDialog(frame, "Select a task to mark complete.");
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

    // CALENDAR TAB
    static JPanel buildCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = new JPanel();
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        header.add(prevBtn);
        header.add(monthLabel);
        header.add(nextBtn);
        panel.add(header, BorderLayout.NORTH);

        calendarGrid = new JPanel(new GridLayout(0, 7));
        panel.add(new JScrollPane(calendarGrid), BorderLayout.CENTER);

        calendarTaskDisplay.setEditable(false);
        calendarTaskDisplay.setRows(5);
        panel.add(new JScrollPane(calendarTaskDisplay), BorderLayout.SOUTH);

        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            renderCalendar(calendarTaskDisplay);
        });

        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            renderCalendar(calendarTaskDisplay);
        });

        renderCalendar(calendarTaskDisplay);
        return panel;
    }

    static void renderCalendar(JTextArea taskDisplay) {
        calendarGrid.removeAll();
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : dayNames) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            calendarGrid.add(lbl);
        }

        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
        LocalDate first = currentMonth.atDay(1);
        int startDay = first.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < startDay; i++) {
            calendarGrid.add(new JLabel(""));
        }

        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            String dateStr = currentMonth.getYear() + "-" +
                String.format("%02d", currentMonth.getMonthValue()) + "-" +
                String.format("%02d", day);

            boolean hasTask = tm.tasks.stream().anyMatch(t -> t.dueDate.equals(dateStr));
            JButton dayBtn = new JButton(String.valueOf(day));

            if (hasTask) {
                dayBtn.setBackground(new Color(255, 220, 100));
                dayBtn.setOpaque(true);
            }

            final String fd = dateStr;
            dayBtn.addActionListener(e -> {
                StringBuilder sb = new StringBuilder("Tasks due on " + fd + ":\n");
                boolean found = false;
                for (Task t : tm.tasks) {
                    if (t.dueDate.equals(fd)) {
                        sb.append("- ").append(t.title).append(" [").append(t.status).append("]\n");
                        found = true;
                    }
                }
                if (!found) sb.append("No tasks due.");
                taskDisplay.setText(sb.toString());
            });

            calendarGrid.add(dayBtn);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    // DEV CONSOLE TAB
    static JPanel buildDevConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel controls = new JPanel();
        JLabel dateLabel = new JLabel("Set Fake Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
        JButton setDateBtn = new JButton("Set Date");

        controls.add(dateLabel);
        controls.add(dateField);
        controls.add(setDateBtn);
        panel.add(controls, BorderLayout.NORTH);

        devLog = new JTextArea();
        devLog.setEditable(false);
        devLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        devLog.setText("Dev Console ready.\nCurrent date: " + fakeToday + "\n");
        panel.add(new JScrollPane(devLog), BorderLayout.CENTER);

        setDateBtn.addActionListener(e -> {
            try {
                fakeToday = LocalDate.parse(dateField.getText().trim());
                devLog.append("Fake date set to: " + fakeToday + "\n");
                devLog.append("Running scheduler...\n");
                for (Task t : tm.tasks) {
                    Status before = t.status;
                    Scheduler.checkAndMarkMissed(tm.tasks, fakeToday);
                    if (t.status != before) {
                        devLog.append("  CHANGED: " + t.title + " -> " + t.status + "\n");
                    }
                }
                Scheduler.sortByDueDate(tm.tasks);
                refreshTable();
                renderCalendar(calendarTaskDisplay);
                devLog.append("Done.\n");
            } catch (Exception ex) {
                devLog.append("Invalid date format. Use YYYY-MM-DD.\n");
            }
        });

        return panel;
    }

    static void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (Task t : tm.tasks) {
            tableModel.addRow(new Object[]{t.title, t.subject, t.dueDate, t.dueTime, t.status});
        }
    }
}