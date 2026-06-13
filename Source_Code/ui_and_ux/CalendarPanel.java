package ui_and_ux;

import core.Task;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import javax.swing.*;

// The "Calendar" tab. Shows a month-by-month grid where days with at least
// one task due are highlighted, and clicking a day lists the tasks due on it.
public class CalendarPanel {

    static JPanel calendarGrid;
    static JLabel monthLabel;
    static YearMonth currentMonth = YearMonth.now();
    static JTextArea taskDisplay = new JTextArea();

    // Builds the calendar tab: header with prev/next month buttons, the
    // day grid, and a text area at the bottom for showing a day's tasks.
    public static JPanel build(ArrayList<Task> tasks) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = new JPanel();
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        // Turning off focusable stops Swing from drawing that dotted focus
        // box around the button after it's clicked.
        prevBtn.setFocusable(false);
        nextBtn.setFocusable(false);
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        header.add(prevBtn);
        header.add(monthLabel);
        header.add(nextBtn);
        panel.add(header, BorderLayout.NORTH);

        calendarGrid = new JPanel(new GridLayout(0, 7));

        // Wrap the grid and the task-list area in padded containers so they
        // match the same 8/12/8/12 spacing used around the table in the
        // Tasks tab, keeping the look consistent across tabs.
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        gridWrapper.add(new JScrollPane(calendarGrid), BorderLayout.CENTER);
        panel.add(gridWrapper, BorderLayout.CENTER);

        taskDisplay.setEditable(false);
        taskDisplay.setRows(5);

        JPanel taskDisplayWrapper = new JPanel(new BorderLayout());
        taskDisplayWrapper.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        taskDisplayWrapper.add(new JScrollPane(taskDisplay), BorderLayout.CENTER);
        panel.add(taskDisplayWrapper, BorderLayout.SOUTH);

        // Move to the previous/next month and redraw the grid.
        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            render(tasks);
        });

        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            render(tasks);
        });

        render(tasks);
        return panel;
    }

    // Redraws the calendar grid for currentMonth. Called on first build,
    // when changing months, and whenever the task list changes (e.g. after
    // syncing, adding, or editing tasks) so the highlights stay accurate.
    public static void render(ArrayList<Task> tasks) {
        calendarGrid.removeAll();

        // Row of day-of-week headers (Sun-Sat).
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : dayNames) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            calendarGrid.add(lbl);
        }

        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
        LocalDate first = currentMonth.atDay(1);
        // getDayOfWeek() returns 1-7 (Mon-Sun); the %7 shifts it so Sunday = 0,
        // matching the column order of our header row above.
        int startDay = first.getDayOfWeek().getValue() % 7;

        // Add empty filler cells so day 1 lines up under the correct weekday.
        for (int i = 0; i < startDay; i++) {
            calendarGrid.add(new JLabel(""));
        }

        // Add one button per day of the month.
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            String dateStr = currentMonth.getYear() + "-" +
                String.format("%02d", currentMonth.getMonthValue()) + "-" +
                String.format("%02d", day);

            boolean hasTask = tasks.stream().anyMatch(t -> t.dueDate.equals(dateStr));
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFocusable(false);

            // Highlight days that have at least one task due.
            if (hasTask) {
                dayBtn.setBackground(new Color(255, 220, 100));
                dayBtn.setOpaque(true);
            }

            // Clicking a day lists all tasks due on that date in the text area below.
            final String fd = dateStr;
            dayBtn.addActionListener(e -> {
                StringBuilder sb = new StringBuilder("Tasks due on " + fd + ":\n");
                boolean found = false;
                for (Task t : tasks) {
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
}