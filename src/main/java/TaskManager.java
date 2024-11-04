import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class TaskManager<T extends Item> {
    private ArrayList<T> tasks = new ArrayList<>();

    public void addTask(T task) {
        tasks.add(task);
    }

    public void saveTasksToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(tasks);
        }
    }

    public void loadTasksFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            tasks = (ArrayList<T>) ois.readObject();
        }
    }

    public ArrayList<T> getTasks() {
        return tasks;
    }

    // Sort tasks by priority if they are of type Task
    public void sortByPriority() {
        tasks.sort((a, b) -> {
            if (a instanceof Task && b instanceof Task) {
                return Integer.compare(((Task) a).getPriority(), ((Task) b).getPriority());
            }
            return 0;
        });
    }

    // Sort tasks by due date if they are of type Task
    public void sortByDueDate() {
        tasks.sort((a, b) -> {
            if (a instanceof Task && b instanceof Task) {
                return ((Task) a).getDueDate().compareTo(((Task) b).getDueDate());
            }
            return 0;
        });
    }
}
