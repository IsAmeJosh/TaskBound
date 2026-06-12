package ui_and_ux;

import core.Status;
import core.Task;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.Scheduler;
import logic.TaskManager;

public class DevConsolePanel {

    static JTextArea devLog;

    public static JPanel build(TaskManager tm, DefaultTableModel tableModel, LocalDate[] fakeTodayRef) {
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
        devLog.setText("Dev Console ready.\nCurrent date: " + fakeTodayRef[0] + "\n");
        panel.add(new JScrollPane(devLog), BorderLayout.CENTER);

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