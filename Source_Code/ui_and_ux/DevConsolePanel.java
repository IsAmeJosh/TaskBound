package ui_and_ux;

import core.Status;
import core.Task;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.Scheduler;
import logic.TaskManager;

// The "Dev Console" tab. This is a testing/debugging tool, not something a
// real user would normally see - it lets us pretend "today" is a different
// date so we can check that overdue/missed logic and the calendar behave
// correctly without waiting for real time to pass.
public class DevConsolePanel {

    static JTextArea devLog;

    // Builds the dev console: a date input + button at the top, and a log
    // area below showing what happened each time the fake date is changed.
    public static JPanel build(TaskManager tm, DefaultTableModel tableModel, LocalDate[] fakeTodayRef) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel controls = new JPanel();
        JLabel dateLabel = new JLabel("Set Fake Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
        JButton setDateBtn = new JButton("Set Date");
        setDateBtn.setFocusable(false);

        controls.add(dateLabel);
        controls.add(dateField);
        controls.add(setDateBtn);
        panel.add(controls, BorderLayout.NORTH);

        devLog = new JTextArea();
        devLog.setEditable(false);
        devLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        devLog.setText("Dev Console ready.\nCurrent date: " + fakeTodayRef[0] + "\n");

        // Same 8/12/8/12 padding as the Tasks and Calendar tabs, so the
        // log area doesn't sit flush against the window edges.
        JPanel logWrapper = new JPanel(new BorderLayout());
        logWrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        logWrapper.add(new JScrollPane(devLog), BorderLayout.CENTER);
        panel.add(logWrapper, BorderLayout.CENTER);

        // When "Set Date" is clicked: update the shared fake-today reference,
        // re-run the missed-task check against the new date, log which tasks
        // changed status, then refresh the Tasks table and Calendar so the
        // whole app reflects the new "today".
        setDateBtn.addActionListener(e -> {
            try {
                fakeTodayRef[0] = LocalDate.parse(dateField.getText().trim());
                devLog.append("Fake date set to: " + fakeTodayRef[0] + "\n");
                devLog.append("Running scheduler...\n");
                for (Task t : tm.tasks) {
                    Status before = t.status;
                    Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                    if (t.status != before) {
                        devLog.append("  CHANGED: " + t.title + " -> " + t.status + "\n");
                    }
                }
                Scheduler.sortByDueDate(tm.tasks);
                TasksPanel.refresh(tm, tableModel, fakeTodayRef[0]);
                CalendarPanel.render(tm.tasks);
                devLog.append("Done.\n");
            } catch (Exception ex) {
                devLog.append("Invalid date format. Use YYYY-MM-DD.\n");
            }
        });

        return panel;
    }
}