package ui_and_ux;

import core.Task;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import javax.swing.*;

public class CalendarPanel {

    static JPanel calendarGrid;
    static JLabel monthLabel;
    static YearMonth currentMonth = YearMonth.now();
    static JTextArea taskDisplay = new JTextArea();

    public static JPanel build(ArrayList<Task> tasks) {
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

        taskDisplay.setEditable(false);
        taskDisplay.setRows(5);
        panel.add(new JScrollPane(taskDisplay), BorderLayout.SOUTH);

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

    public static void render(ArrayList<Task> tasks) {
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

            boolean hasTask = tasks.stream().anyMatch(t -> t.dueDate.equals(dateStr));
            JButton dayBtn = new JButton(String.valueOf(day));

            if (hasTask) {
                dayBtn.setBackground(new Color(255, 220, 100));
                dayBtn.setOpaque(true);
            }

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