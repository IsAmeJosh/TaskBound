package data;

import core.Status;
import core.Task;
import java.io.*;
import java.util.ArrayList;

// Handles saving and loading tasks to/from a CSV file on disk. We always
// store times in 24hr format in the file (since it's unambiguous and easy
// to parse back), but display them in 12hr format in the UI.
public class FileHandler {

    // Writes every task out as one CSV line: title, subject, due date,
    // due time (in 24hr format), and status, to the given file.
    public static void saveTasks(ArrayList<Task> tasks, File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        for (Task t : tasks) {
            String time24 = to24Hour(t.dueTime);
            fw.write(t.title + "," + t.subject + "," + t.dueDate + "," + time24 + "," + t.status + "\n");
        }
        fw.close();
    }

    // Reads tasks back from the given CSV file. Lines with fewer than 5
    // fields are skipped (likely leftover blank/corrupt lines) rather than
    // crashing.
    public static ArrayList<Task> loadTasks(File file) throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
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

    // Converts a "hh:mm AM/PM" string into "HH:mm" for storage. If the string
    // doesn't end in AM/PM, we assume it's already in 24hr format and leave
    // it as-is.
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