import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

// TaskManager class for managing tasks
public class TaskManager<T extends Task> {
    private List<T> tasks;
    private final TaskHistoryManager<T> historyManager;  // Add history manager

    // Constructor
    public TaskManager() {
        tasks = new LinkedList<>();
        historyManager = new TaskHistoryManager<>();  // Initialize history manager
    }

    // Add a task
    public void addTask(T task) {
        tasks.add(task);
        historyManager.addToHistory(task, "Add");  // Record the add operation
    }

    // Remove a task
    public void removeTask(T task) {
        tasks.remove(task);
        historyManager.addToHistory(task, "Remove");  // Record the remove operation
    }

    // Get all tasks
    public List<T> getTasks() {
        return tasks;
    }

    // Sort tasks by priority using Collections.sort()
    public void sortTasksByPriority() {
        tasks.sort(Comparator.comparingInt(Task::getPriority));
    }

    // Sort tasks by due date using Collections.sort()
    public void sortTasksByDueDate() {
        tasks.sort(Comparator.comparing(Task::getDueDate));
    }

    // Save tasks to file
    public void saveTasksToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(tasks);
        }
    }

    // Load tasks from file
    public void loadTasksFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            tasks = (List<T>) in.readObject();
        }
    }

    // Undo last task action
    public void undo() {
        TaskHistoryManager.HistoryRecord<T> lastAction = historyManager.undo();
        if (lastAction != null) {
            if (lastAction.operation().equals("Add")) {
                tasks.remove(lastAction.task());
            } else if (lastAction.operation().equals("Remove")) {
                tasks.add(lastAction.task());
            }
        }
    }

    // Redo last undone task
    public void redo() {
        TaskHistoryManager.HistoryRecord<T> lastUndone = historyManager.redo();
        if (lastUndone != null) {
            if (lastUndone.operation().equals("Add")) {
                tasks.add(lastUndone.task());
            } else if (lastUndone.operation().equals("Remove")) {
                tasks.remove(lastUndone.task());
            }
        }
    }

}