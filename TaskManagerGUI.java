import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.table.*;

// This is the main class — it runs the whole Task Manager app
public class TaskManagerGUI {

    // A list that stores all the tasks the user adds
    private static ArrayList<Task> tasks = new ArrayList<>();

    // Controls what data shows up in the table on screen
    private static DefaultTableModel tableModel;

    // Used to format dates internally as "yyyy-MM-dd HH:mm"
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // --- main() — this is where the program starts ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> buildWindow());
    }

    // --- buildWindow() — creates and displays the main app window ---
    static void buildWindow() {

        JFrame frame = new JFrame("Task Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(30, 30, 40));

        // Title label at the top
        JLabel title = new JLabel("  Task Manager", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(20, 20, 30));
        title.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        frame.add(title, BorderLayout.NORTH);

        // Table columns
        String[] columns = {"#", "Subject", "Task Description", "Due Date", "Alarm", "Status", "Overdue"};

        // Table model — holds all the row data, cells are not directly editable
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        // Style the table
        JTable table = new JTable(tableModel);
        table.setBackground(new Color(40, 40, 55));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setBackground(new Color(60, 60, 80));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionBackground(new Color(80, 100, 180));
        table.setGridColor(new Color(60, 60, 75));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(new Color(30, 30, 40));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        frame.add(scrollPane, BorderLayout.CENTER);

        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(20, 20, 30));

        // Create all buttons using the makeButton() helper
        JButton btnAdd         = makeButton("Add Task",      new Color(50, 150, 80));
        JButton btnRemove      = makeButton("Remove",         new Color(180, 60, 60));
        JButton btnStatus      = makeButton("Set Status",    new Color(70, 100, 180));
        JButton btnSetAlarm    = makeButton("Set Alarm",     new Color(160, 120, 30));
        JButton btnCancelAlarm = makeButton("Cancel Alarm",  new Color(100, 60, 130));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRemove);
        buttonPanel.add(btnStatus);
        buttonPanel.add(btnSetAlarm);
        buttonPanel.add(btnCancelAlarm);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // ============================================================
        // Button Actions
        // ============================================================

        // ADD TASK — opens a form with dropdowns; creates a new task when OK is clicked
        btnAdd.addActionListener(e -> {
            JTextField subjectField = new JTextField();
            JTextField descField    = new JTextField();

            // Month names for the dropdown
            String[] months = {"January","February","March","April","May","June",
                                "July","August","September","October","November","December"};

            // Day options 01 to 31
            String[] days = new String[31];
            for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);

            // Year options: this year up to 5 years from now
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            String[] years = new String[6];
            for (int i = 0; i < 6; i++) years[i] = String.valueOf(currentYear + i);

            // Create the date dropdowns (JComboBox = dropdown in Java)
            JComboBox<String> monthBox = new JComboBox<>(months);
            JComboBox<String> dayBox   = new JComboBox<>(days);
            JComboBox<String> yearBox  = new JComboBox<>(years);

            // JSpinner = a scroll box with up/down arrows
            // SpinnerNumberModel(start, min, max, step)
            JSpinner hourSpinner   = new JSpinner(new SpinnerNumberModel(12, 1, 12, 1));  // 1–12
            JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, -1, 60, 1));  // -1 and 60 let the listener catch the wrap

            // Format minute to always show 2 digits (e.g. 05 not 5)
            JSpinner.NumberEditor minEditor = new JSpinner.NumberEditor(minuteSpinner, "00");
            minuteSpinner.setEditor(minEditor);

            // Set a fixed width so spinners look clean
            hourSpinner.setPreferredSize(new Dimension(60, 28));
            minuteSpinner.setPreferredSize(new Dimension(60, 28));

            // Disable typing — only up/down arrows allowed
            ((JSpinner.DefaultEditor) hourSpinner.getEditor()).getTextField().setEditable(false);
            ((JSpinner.DefaultEditor) minuteSpinner.getEditor()).getTextField().setEditable(false);

            // Wrap-around for minutes: going below 0 jumps to 59, going above 59 jumps to 0
            minuteSpinner.addChangeListener(ev -> {
                int val = (int) minuteSpinner.getValue();
                if (val < 0)  minuteSpinner.setValue(59); // went below 0 → wrap to 59
                if (val > 59) minuteSpinner.setValue(0);  // went above 59 → wrap to 0
            });

            // AM/PM stays as a dropdown since there are only 2 options
            JComboBox<String> ampmBox = new JComboBox<>(new String[]{"AM", "PM"});

            // Date dropdowns in one row
            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            datePanel.add(monthBox);
            datePanel.add(dayBox);
            datePanel.add(yearBox);

            // Time row: hour spinner : minute spinner + AM/PM dropdown
            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            timePanel.add(new JLabel("Hour:"));
            timePanel.add(hourSpinner);
            timePanel.add(new JLabel("Min:"));
            timePanel.add(minuteSpinner);
            timePanel.add(ampmBox);

            Object[] fields = {
                "Subject:",  subjectField,
                "Task:",     descField,
                "Due Date:", datePanel,
                "Due Time:", timePanel
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Add New Task", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String subject = subjectField.getText().trim();
                String desc    = descField.getText().trim();

                if (subject.isEmpty() || desc.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please fill in Subject and Task.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Convert dropdowns + spinners to a formatted date string and add the task
                String due = TaskManagerGUI.buildDateString(yearBox, monthBox, dayBox, hourSpinner, minuteSpinner, ampmBox);
                tasks.add(new Task(subject, desc, due));
                refreshTable();
            }
        });

        // REMOVE — deletes the selected task after a confirmation popup
        btnRemove.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a task to remove.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(frame, "Remove this task?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tasks.get(selected).cancelAlarm();
                tasks.remove(selected);
                refreshTable();
            }
        });

        // SET STATUS — lets the user pick Not Opened, On Going, or Completed
        btnStatus.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a task first.");
                return;
            }

            String[] options = {"Not Opened", "On Going", "Completed"};
            String choice = (String) JOptionPane.showInputDialog(
                frame, "Select new status:", "Set Status",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]
            );

            if (choice == null) return;

            Task t = tasks.get(selected);
            if (choice.equals("Not Opened")) t.markAsNotOpened();
            else if (choice.equals("On Going")) t.markAsOnGoing();
            else if (choice.equals("Completed")) t.markAsCompleted();

            refreshTable();
        });

        // SET ALARM — same dropdown form as Add Task, schedules a reminder popup
        btnSetAlarm.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a task first.");
                return;
            }

            String[] months = {"January","February","March","April","May","June",
                               "July","August","September","October","November","December"};
            String[] days = new String[31];
            for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);

            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            String[] years = new String[6];
            for (int i = 0; i < 6; i++) years[i] = String.valueOf(currentYear + i);

            String[] hours = new String[12]; // kept for reference but not used — spinners replace this
            String[] minutes = new String[60];

            JComboBox<String> monthBox  = new JComboBox<>(months);
            JComboBox<String> dayBox    = new JComboBox<>(days);
            JComboBox<String> yearBox   = new JComboBox<>(years);

            // Spinners for hour and minute with up/down arrows
            JSpinner hourSpinner   = new JSpinner(new SpinnerNumberModel(12, 1, 12, 1));
            JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, -1, 60, 1)); // -1 and 60 let the listener catch the wrap

            // Always show 2 digits for minutes
            JSpinner.NumberEditor minEditor = new JSpinner.NumberEditor(minuteSpinner, "00");
            minuteSpinner.setEditor(minEditor);

            hourSpinner.setPreferredSize(new Dimension(60, 28));
            minuteSpinner.setPreferredSize(new Dimension(60, 28));

            // Disable typing — only up/down arrows allowed
            ((JSpinner.DefaultEditor) hourSpinner.getEditor()).getTextField().setEditable(false);
            ((JSpinner.DefaultEditor) minuteSpinner.getEditor()).getTextField().setEditable(false);

            // Wrap-around for minutes: going below 0 jumps to 59, going above 59 jumps to 0
            minuteSpinner.addChangeListener(ev -> {
                int val = (int) minuteSpinner.getValue();
                if (val < 0)  minuteSpinner.setValue(59);
                if (val > 59) minuteSpinner.setValue(0);
            });

            JComboBox<String> ampmBox = new JComboBox<>(new String[]{"AM", "PM"});

            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            datePanel.add(monthBox);
            datePanel.add(dayBox);
            datePanel.add(yearBox);

            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            timePanel.add(new JLabel("Hour:"));
            timePanel.add(hourSpinner);
            timePanel.add(new JLabel("Min:"));
            timePanel.add(minuteSpinner);
            timePanel.add(ampmBox);

            Object[] fields = {
                "Alarm Date:", datePanel,
                "Alarm Time:", timePanel
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Set Alarm", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String alarmStr = TaskManagerGUI.buildDateString(yearBox, monthBox, dayBox, hourSpinner, minuteSpinner, ampmBox);
            tasks.get(selected).setAlarm(alarmStr, frame);
            refreshTable();
        });

        // CANCEL ALARM — removes the alarm from the selected task
        btnCancelAlarm.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a task first.");
                return;
            }
            tasks.get(selected).cancelAlarm();
            refreshTable();
        });

        frame.setVisible(true);
    }

    // --- refreshTable() — clears and redraws the table with the latest task data ---
    static void refreshTable() {
        tableModel.setRowCount(0);

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            tableModel.addRow(new Object[]{
                i + 1,
                t.getSubject(),
                t.getTaskDescription(),
                t.getDisplayDate(),          // Shows date in 12-hour SST format
                t.getAlarmDateTime() != null ? t.getDisplayAlarm() : "Not set",
                t.getStatusLabel(),
                t.isOverdue() ? "Yes" : "No"
            });
        }
    }

    // --- makeButton() — creates a styled button; avoids repeating styling code ---
    static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 38));
        return btn;
    }

    // --- buildDateString() — converts dropdowns + spinners into an internal date string ---
    // Hour and Minute now come from JSpinners (up/down scroll boxes)
    // Converts 12-hour time to 24-hour internally so Java can parse it correctly
    // Example: June 3 2026, 11:59 PM -> "2026-06-03 23:59"
    static String buildDateString(JComboBox<String> yearBox, JComboBox<String> monthBox,
                                  JComboBox<String> dayBox,  JSpinner hourSpinner,
                                  JSpinner minuteSpinner,    JComboBox<String> ampmBox) {
        String year   = (String) yearBox.getSelectedItem();
        String month  = String.format("%02d", monthBox.getSelectedIndex() + 1);
        String day    = (String) dayBox.getSelectedItem();
        int hour12    = (int) hourSpinner.getValue();    // getValue() reads the current spinner number
        int minute    = (int) minuteSpinner.getValue();
        String period = (String) ampmBox.getSelectedItem();

        // Convert 12-hour to 24-hour so Java's date parser works correctly
        int hour24;
        if (period.equals("AM")) {
            hour24 = (hour12 == 12) ? 0 : hour12;           // 12 AM = 00:xx
        } else {
            hour24 = (hour12 == 12) ? 12 : hour12 + 12;     // 12 PM = 12:xx, 1 PM = 13:xx
        }

        return year + "-" + month + "-" + day + " " + String.format("%02d", hour24) + ":" + String.format("%02d", minute);
    }


    // ============================================================
    // Task class — represents a single task with all its details
    // ============================================================
    static class Task {

        // The three possible progress states of a task
        enum Status { NOT_OPENED, ON_GOING, COMPLETED }

        private String subject;
        private String taskDescription;
        private String dueDate;       // Stored internally as "yyyy-MM-dd HH:mm" (24-hour)
        private String alarmDateTime; // null means no alarm is set
        private Status status;
        private Timer alarmTimer;     // the background timer that fires the alarm

        // --- Constructor — sets default status to NOT_OPENED when task is first created ---
        Task(String subject, String taskDescription, String dueDate) {
            this.subject = subject;
            this.taskDescription = taskDescription;
            this.dueDate = dueDate;
            this.status = Status.NOT_OPENED;
            this.alarmTimer = null;
        }

        // --- Getters ---
        String getSubject()         { return subject; }
        String getTaskDescription() { return taskDescription; }
        String getDueDate()         { return dueDate; }
        String getAlarmDateTime()   { return alarmDateTime; }
        Status getStatus()          { return status; }

        // --- getDisplayDate() — formats the due date for the table in 12-hour SST format ---
        // Example output: "Jun 3, 2026 11:59 PM (SST)"
        String getDisplayDate() {
            try {
                SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                SimpleDateFormat out = new SimpleDateFormat("MMM d, yyyy hh:mm a '(SST)'");
                return out.format(in.parse(dueDate));
            } catch (ParseException e) {
                return dueDate; // If formatting fails, just show the raw string
            }
        }

        // --- getDisplayAlarm() — formats the alarm time for the table in 12-hour SST format ---
        String getDisplayAlarm() {
            try {
                SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                SimpleDateFormat out = new SimpleDateFormat("MMM d, yyyy hh:mm a '(SST)'");
                return out.format(in.parse(alarmDateTime));
            } catch (ParseException e) {
                return alarmDateTime;
            }
        }

        // --- Status changers ---
        void markAsNotOpened() { status = Status.NOT_OPENED; }
        void markAsOnGoing()   { status = Status.ON_GOING; }
        void markAsCompleted() { status = Status.COMPLETED; }

        // --- getStatusLabel() — returns the status as readable text for the table ---
        String getStatusLabel() {
            switch (status) {
                case NOT_OPENED: return "Not Opened";
                case ON_GOING:   return "On Going";
                case COMPLETED:  return "Completed";
                default:         return "Unknown";
            }
        }

        // --- isOverdue() — returns true if past due date and task is not completed ---
        boolean isOverdue() {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date due = sdf.parse(dueDate);
                return new Date().after(due) && status != Status.COMPLETED;
            } catch (ParseException e) {
                return false;
            }
        }

        // --- setAlarm() — schedules a reminder popup at the chosen date and time ---
        void setAlarm(String alarmDateTimeStr, JFrame frame) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date alarmDate = sdf.parse(alarmDateTimeStr);

                if (alarmDate.before(new Date())) {
                    JOptionPane.showMessageDialog(frame, "Alarm time is in the past!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                this.alarmDateTime = alarmDateTimeStr;

                if (alarmTimer != null) alarmTimer.cancel();

                alarmTimer = new Timer();
                alarmTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // invokeLater makes sure the popup runs on the GUI thread
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame,
                                "ALARM!\n\nSubject: " + subject +
                                "\nTask: " + taskDescription +
                                "\nDue: " + getDisplayDate(),
                                "Task Reminder", JOptionPane.WARNING_MESSAGE);
                        });
                    }
                }, alarmDate);

            } catch (ParseException e) {
                JOptionPane.showMessageDialog(frame, "Invalid date format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // --- cancelAlarm() — stops and removes the alarm for this task ---
        void cancelAlarm() {
            if (alarmTimer != null) {
                alarmTimer.cancel();
                alarmTimer = null;
            }
            alarmDateTime = null;
        }
    }
}