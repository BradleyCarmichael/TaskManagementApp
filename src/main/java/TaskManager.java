import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class TaskManager {
    private ArrayList<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void saveTasksToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(tasks);
        }
    }

    public void loadTasksFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            tasks = (ArrayList<Task>) ois.readObject();
        }
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    // Sort tasks by priority
    public void sortByPriority() {
        tasks.sort(Comparator.comparingInt(Task::getPriority));
    }

    // Sort tasks by due date
    public void sortByDueDate() {
        tasks.sort(Comparator.comparing(Task::getDueDate));
    }
}
