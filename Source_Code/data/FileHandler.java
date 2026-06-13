package data;

import core.Task;
import core.Status;
import java.io.*;
import java.util.ArrayList;

public class FileHandler {

    // Save time in 24hr format to keep CSV clean
    public static void saveTasks(ArrayList<Task> tasks) throws IOException {
        FileWriter fw = new FileWriter("tasks.csv");
        for (Task t : tasks) {
            String time24 = to24Hour(t.dueTime);
            fw.write(t.title + "," + t.subject + "," + t.dueDate + "," + time24 + "," + t.status + "\n");
        }
        fw.close();
    }

    // Load tasks and convert time to 12hr for display
    public static ArrayList<Task> loadTasks() throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("tasks.csv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < 5) continue;
            Task t = new Task();
            t.title = parts[0];
            t.subject = parts[1];
            t.dueDate = parts[2];
            t.dueTime = parts[3];
            t.status = Status.valueOf(parts[4].trim());
            tasks.add(t);
        }
        br.close();
        return tasks;
    }

    // Convert "hh:mm AM/PM" to "HH:mm" for storage
    static String to24Hour(String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) return "";
        String s = dueTime.trim();
        String up = s.toUpperCase();
        if (!up.endsWith("AM") && !up.endsWith("PM")) return s;
        try {
            String[] tok = s.split("\\s+");
            String timePart = tok[0];
            String ampm = tok[tok.length - 1].toUpperCase();
            String[] hm = timePart.split(":");
            int hh = Integer.parseInt(hm[0].trim());
            int mm = Integer.parseInt(hm[1].trim());
            if (ampm.equals("PM") && hh < 12) hh += 12;
            if (ampm.equals("AM") && hh == 12) hh = 0;
            return String.format("%02d:%02d", hh, mm);
        } catch (Exception e) {
            return s;
        }
    }
}