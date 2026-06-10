package ui_and_ux;

import core.Task;
import data.FileHandler;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logic.LMSMockData;
import logic.TaskManager;

public class TaskManagerGUI {

    static TaskManager tm = new TaskManager();
    static DefaultTableModel tableModel;

    public static void main(String[] args) throws Exception {
        try {
            tm.tasks = FileHandler.loadTasks();
            tm.tasks.removeIf(t -> t.title == null || t.title.trim().isEmpty());
        } catch (Exception e) {
            // No saved file yet, start empty
        }

        JFrame frame = new JFrame("TaskBound");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLayout(new BorderLayout());

        String[] columns = {"Title", "Subject", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        refreshTable();

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton syncBtn = new JButton("Sync LMS");
        JButton addBtn = new JButton("Add Task");
        JButton deleteBtn = new JButton("Delete Task");
        JButton saveBtn = new JButton("Save");

        buttonPanel.add(syncBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(saveBtn);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        syncBtn.addActionListener(e -> {
            tm.tasks = LMSMockData.getFakeTasks();
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

            JComboBox<String> yearBox = new JComboBox<>(years);
            JComboBox<String> monthBox = new JComboBox<>(months);
            JComboBox<String> dayBox = new JComboBox<>(days);

            JPanel datePanel = new JPanel();
            datePanel.add(yearBox);
            datePanel.add(new JLabel("-"));
            datePanel.add(monthBox);
            datePanel.add(new JLabel("-"));
            datePanel.add(dayBox);

            Object[] fields = {
                "Title:", titleField,
                "Subject:", subjectField,
                "Due Date:", datePanel
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
                t.status = "INCOMPLETE";
                tm.addTask(t);
                refreshTable();
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tm.tasks.remove(row);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(frame, "Select a task to delete.");
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

        frame.setVisible(true);
    }

    static void refreshTable() {
        tableModel.setRowCount(0);
        for (Task t : tm.tasks) {
            tableModel.addRow(new Object[]{t.title, t.subject, t.dueDate, t.status});
        }
    }
}