import java.io.*;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class TaskManager<T extends Task> {
    private List<T> tasks;  // Change to ArrayList
    private final TaskHistoryManager<T> historyManager;

    // Constructor
    public TaskManager() {
        tasks = new ArrayList<>();  // Change to ArrayList
        historyManager = new TaskHistoryManager<>();
    }

    // Add a task
    public void addTask(T task) {
        tasks.add(task);
        historyManager.addToHistory(task, "Add");
    }

    // Remove a task
    public void removeTask(T task) {
        tasks.remove(task);
        historyManager.addToHistory(task, "Remove");
    }

    // Get all tasks
    public List<T> getTasks() {
        return tasks;
    }

    // Sort tasks by priority using quicksort
    public void sortTasksByPriority() {
        quicksort(tasks, 0, tasks.size() - 1, (a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
    }

    // Sort tasks by due date using quicksort
    public void sortTasksByDueDate() {
        quicksort(tasks, 0, tasks.size() - 1, (a, b) -> a.getDueDate().compareTo(b.getDueDate()));
    }

    // Quicksort implementation
    private void quicksort(List<T> list, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, comparator);
            quicksort(list, low, pivotIndex - 1, comparator); // Sort left side
            quicksort(list, pivotIndex + 1, high, comparator); // Sort right side
        }
    }

    // Partition method for quicksort
    private int partition(List<T> list, int low, int high, Comparator<T> comparator) {
        T pivot = list.get(high); // Choose the last element as pivot
        int i = low - 1; // Index of the smaller element

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                // Swap elements
                T temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
        // Swap pivot into its correct position
        T temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);

        return i + 1;
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

