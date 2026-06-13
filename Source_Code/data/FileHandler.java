package data;

import core.Status;
import core.Task;
import java.io.*;
import java.util.ArrayList;
import logic.TimeConverter;

/* Handles saving and loading tasks to and from a CSV file on disk.
   Times are always stored in 24hr format in the file since it is
   unambiguous and easy to parse back. The UI always displays 12hr. */
public class FileHandler {

    /* Writes every task out as one CSV line per task:
       title, subject, due date, due time in 24hr, and status.
       Saves to whichever File the user picked via the file chooser. */
    public static void saveTasks(ArrayList<Task> tasks, File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        for (Task t : tasks) {
            String time24 = TimeConverter.to24Hour(t.dueTime);
            fw.write(t.title + "," + t.subject + "," + t.dueDate + "," + time24 + "," + t.status + "\n");
        }
        fw.close();
    }

    /* Reads tasks back from the given CSV file.
       Lines with fewer than 5 fields are skipped to handle
       blank or corrupt lines without crashing. */
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
}