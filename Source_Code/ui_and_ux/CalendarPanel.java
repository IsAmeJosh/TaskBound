package ui_and_ux;

import core.Status;
import core.Task;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import javax.swing.*;

/* The Calendar tab. Shows a monthly grid where days with at least
   one task due are highlighted. Color reflects the overall status
   of tasks due on that day: yellow for incomplete, green for mostly
   complete, red for mostly missed. Clicking a day lists its tasks. */
public class CalendarPanel {

    static JPanel calendarGrid;
    static JLabel monthLabel;
    static YearMonth currentMonth = YearMonth.now();
    static JTextArea taskDisplay = new JTextArea();

    /* Builds the calendar tab: month navigation header, the day grid,
       and a task list area at the bottom. */
    public static JPanel build(ArrayList<Task> tasks) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = new JPanel();
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        prevBtn.setFocusable(false);
        nextBtn.setFocusable(false);
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        header.add(prevBtn);
        header.add(monthLabel);
        header.add(nextBtn);
        panel.add(header, BorderLayout.NORTH);

        /* Grid gap of 1px keeps lines thin and consistent. */
        calendarGrid = new JPanel(new GridLayout(0, 7, 1, 1));

        /* Same 8/12 padding as the Tasks tab for visual consistency. */
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        /* Remove the scroll pane border so it does not add thick lines
           around the grid on top of the existing cell borders. */
        JScrollPane calendarScroll = new JScrollPane(calendarGrid);
        calendarScroll.setBorder(BorderFactory.createEmptyBorder());
        calendarScroll.getViewport().setBorder(null);
        gridWrapper.add(calendarScroll, BorderLayout.CENTER);
        panel.add(gridWrapper, BorderLayout.CENTER);

        taskDisplay.setEditable(false);
        taskDisplay.setRows(5);
        JPanel taskDisplayWrapper = new JPanel(new BorderLayout());
        taskDisplayWrapper.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        taskDisplayWrapper.add(new JScrollPane(taskDisplay), BorderLayout.CENTER);
        panel.add(taskDisplayWrapper, BorderLayout.SOUTH);

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

    /* Converts a month enum name like JUNE to title case June. */
    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.charAt(0) + input.substring(1).toLowerCase();
    }

    /* Works out what background color a day cell should be based on
       the tasks due on that date. Returns null if no tasks on that day.
       Yellow = most tasks are incomplete.
       Green  = most tasks are complete.
       Red    = most tasks are missed.
       On a tie, complete wins over missed, incomplete wins over missed. */
    private static Color getDayColor(String dateStr, ArrayList<Task> tasks) {
        int incomplete = 0, complete = 0, missed = 0;
        for (Task t : tasks) {
            if (t.dueDate.equals(dateStr)) {
                if (t.status == Status.INCOMPLETE) incomplete++;
                else if (t.status == Status.COMPLETE) complete++;
                else if (t.status == Status.MISSED) missed++;
            }
        }
        int total = incomplete + complete + missed;
        if (total == 0) return null;

        if (complete >= incomplete && complete >= missed) {
            return new Color(182, 255, 182);
        } else if (missed > incomplete && missed > complete) {
            return new Color(255, 182, 182);
        } else {
            return new Color(255, 220, 100);
        }
    }

    /* Redraws the grid for currentMonth. Called on first build, when
       changing months, and whenever the task list changes so highlights
       stay accurate. */
    public static void render(ArrayList<Task> tasks) {
        calendarGrid.removeAll();

        /* Row of day-of-week headers. */
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : dayNames) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            calendarGrid.add(lbl);
        }

        /* Display month name in title case instead of all caps. */
        String monthName = toTitleCase(currentMonth.getMonth().toString());
        monthLabel.setText(monthName + " " + currentMonth.getYear());

        LocalDate first = currentMonth.atDay(1);

        /* getDayOfWeek returns 1-7 Mon-Sun. The mod 7 shifts it so
           Sunday becomes 0, matching our Sun-Sat header order. */
        int startDay = first.getDayOfWeek().getValue() % 7;

        /* Empty filler cells so day 1 lines up under the right weekday. */
        for (int i = 0; i < startDay; i++) {
            calendarGrid.add(new JLabel(""));
        }

        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            String dateStr = currentMonth.getYear() + "-" +
                String.format("%02d", currentMonth.getMonthValue()) + "-" +
                String.format("%02d", day);

            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFocusable(false);

            /* Color the day cell based on the status mix of its tasks. */
            Color dayColor = getDayColor(dateStr, tasks);
            if (dayColor != null) {
                dayBtn.setBackground(dayColor);
                dayBtn.setOpaque(true);
            }

            /* Clicking a day populates the text area with tasks due on it. */
            final String fd = dateStr;
            dayBtn.addActionListener(e -> {
                StringBuilder sb = new StringBuilder("Tasks due on " + fd + ":\n");
                boolean found = false;
                for (Task t : tasks) {
                    if (t.dueDate.equals(fd)) {
                        sb.append("- ").append(t.title)
                          .append(" [").append(t.status).append("]\n");
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