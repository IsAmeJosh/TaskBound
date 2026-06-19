package ui_and_ux;

import core.Task;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.Scheduler;
import logic.TaskManager;
import logic.TaskSorter;

/* The Dev Console tab. A testing tool that lets us pretend today
   is a different date so we can verify that missed-task detection,
   the calendar highlights, and the time-left column all behave
   correctly without waiting for real time to pass.
   Controls sit in labeled panels at the top, the log sits at the bottom.
   New control panels can be added to the top section as needed. */
public class DevConsolePanel {

    /* Public so TaskButtons can write to the log from button actions. */
    public static JTextArea devLog;

    /* Clears the log and sets new content.
       Called every time an action happens so old output is replaced. */
    public static void setLog(String content) {
        if (devLog != null) devLog.setText(content);
    }

    /* Builds the dev console: a top section for controls arranged in
       labeled panels, and a scrollable log area at the bottom. */
    public static JPanel build(JFrame frame, TaskManager tm, DefaultTableModel tableModel, LocalDate[] fakeTodayRef) {
        JPanel panel = new JPanel(new BorderLayout());

        /* ---- Top section: holds all control panels ---- */
        JPanel controlsSection = new JPanel();
        controlsSection.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlsSection.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));

        /* ---- Fake Date Setter: its own titled bordered panel ---- */
        JPanel dateSetterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        dateSetterPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            "Fake Date"
        ));

        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
        JButton setDateBtn = new JButton("Set Date");
        setDateBtn.setFocusable(false);

        dateSetterPanel.add(dateLabel);
        dateSetterPanel.add(dateField);
        dateSetterPanel.add(setDateBtn);

        controlsSection.add(dateSetterPanel);
        panel.add(controlsSection, BorderLayout.NORTH);

        /* ---- Bottom section: the output log ---- */
        devLog = new JTextArea();
        devLog.setEditable(false);
        devLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        devLog.setText("Dev Console ready.\nCurrent date: " + fakeTodayRef[0] + "\n");

        JPanel logWrapper = new JPanel(new BorderLayout());
        logWrapper.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));
        logWrapper.add(new JScrollPane(devLog), BorderLayout.CENTER);
        panel.add(logWrapper, BorderLayout.CENTER);

        /* When Set Date is clicked: update the fake date, run the scheduler,
           and replace the log with fresh output. */
        setDateBtn.addActionListener(e -> {
            try {
                fakeTodayRef[0] = LocalDate.parse(dateField.getText().trim());

                ArrayList<Task> newlyMissed = Scheduler.checkAndMarkMissed(tm.tasks, fakeTodayRef[0]);
                TaskSorter.sort(tm.tasks);
                TasksPanel.refresh(tm, tableModel, fakeTodayRef[0]);
                CalendarPanel.render(tm.tasks);

                StringBuilder sb = new StringBuilder();
                sb.append("Date: ").append(fakeTodayRef[0]).append("\n");
                sb.append("Scheduler ran.\n");
                if (newlyMissed.isEmpty()) {
                    sb.append("No changes.\n");
                } else {
                    for (Task t : newlyMissed) {
                        sb.append("CHANGED: ").append(t.title).append(" -> MISSED\n");
                    }
                }
                sb.append("Done.\n");
                setLog(sb.toString());

                TaskDialogs.showNewlyMissedPopup(frame, newlyMissed);
            } catch (Exception ex) {
                setLog("Invalid date format. Use YYYY-MM-DD.\n");
            }
        });

        return panel;
    }
}