package data;

import core.Task;
import core.Status;
import java.io.*;
import java.util.ArrayList;

public class FileHandler {

    public static void saveTasks(ArrayList<Task> tasks) throws IOException {
        FileWriter fw = new FileWriter("tasks.csv");
        for (Task t : tasks) {
            fw.write(t.title + "," + t.subject + "," + t.dueDate + "," + t.dueTime + "," + t.status + "\n");
        }
        fw.close();
    }

    public static ArrayList<Task> loadTasks() throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("tasks.csv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            Task t = new Task();
            t.title = parts[0];
            t.subject = parts[1];
            t.dueDate = parts[2];
            t.dueTime = parts[3];
            t.status = Status.valueOf(parts[4]);
            tasks.add(t);
        }
        br.close();
        return tasks;
    }
}