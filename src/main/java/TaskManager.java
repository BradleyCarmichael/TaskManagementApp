import java.io.*;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class TaskManager<T extends Task> {
    private CustomLinkedList<T> tasks;
    private final TaskHistoryManager<T> historyManager;

    // Constructor
    public TaskManager() {
        tasks = new CustomLinkedList<>(); // Initialize CustomLinkedList
        historyManager = new TaskHistoryManager<>();
    }

    // Add a task
    public void addTask(T task) {
        tasks.add(task);
        historyManager.addToHistory(task, "Add");
    }

    // Remove a task
    public void removeTask(T task) {
        int index = findTaskIndex(task);
        if (index != -1) {
            tasks.remove(index);
            historyManager.addToHistory(task, "Remove");
        }
    }

    // Get all tasks
    public List<T> getTasks() {
        List<T> result = new ArrayList<>(); // Create a temporary list to return all tasks
        for (int i = 0; i < tasks.size(); i++) {
            result.add(tasks.get(i));
        }
        return result;
    }

    // Sort tasks by priority using quicksort
    public void sortTasksByPriority() {
        List<T> taskList = getTasks(); // Copy tasks to a temporary list
        quicksort(taskList, 0, taskList.size() - 1, (a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
        reloadTasksFromList(taskList); // Reload sorted tasks back into CustomLinkedList
    }

    // Sort tasks by due date using quicksort
    public void sortTasksByDueDate() {
        List<T> taskList = getTasks(); // Copy tasks to a temporary list
        quicksort(taskList, 0, taskList.size() - 1, (a, b) -> a.getDueDate().compareTo(b.getDueDate()));
        reloadTasksFromList(taskList); // Reload sorted tasks back into CustomLinkedList
    }

    // Quicksort implementation
    private void quicksort(List<T> list, int low, int high, Comparator<T> comparator) {

        // Time complexity: O(n log n) on average and O(n^2) in the worst case
        // The quicksort algorithm partitions the list and recursively sorts the left and right partitions.
        // It is good for large datasets, but can degrade in the worst-case if the pivot is poorly chosen.

        if (low < high) {
            int pivotIndex = partition(list, low, high, comparator);
            quicksort(list, low, pivotIndex - 1, comparator);  // Recursively sort the left half
            quicksort(list, pivotIndex + 1, high, comparator); // Recursively sort the right half
        }
    }

    // Partition method for quicksort
    private int partition(List<T> list, int low, int high, Comparator<T> comparator) {

        // Time complexity: O(n) for each partitioning step
        // This method selects a pivot and rearranges elements so that all elements less than the pivot
        // are on the left, and all elements greater than the pivot are on the right.

        T pivot = list.get(high); // Pivot element
        int i = low - 1;

        // Iterate through the list and rearrange elements
        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                T temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        T temp = list.get(i + 1);
        list.set(i + 1, list.get(high)); // Move the pivot element to its correct position
        list.set(high, temp);

        return i + 1; // Return the index of the pivot
    }

    // Find the index of a task
    private int findTaskIndex(T task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).equals(task)) {
                return i;
            }
        }
        return -1; // Return -1 if the task is not found
    }

    // Reload tasks from a sorted list back into CustomLinkedList
    private void reloadTasksFromList(List<T> taskList) {
        tasks = new CustomLinkedList<>(); // Reinitialize the tasks list
        for (T task : taskList) {
            tasks.add(task); // Add each task from the sorted list to the CustomLinkedList
        }
    }

    // Save tasks to file
    public void saveTasksToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(getTasks()); // Serialize and save the tasks list
        }
    }

    // Load tasks from file
    public void loadTasksFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            List<T> loadedTasks = (List<T>) in.readObject(); // Deserialize tasks from file
            reloadTasksFromList(loadedTasks); // Reload the tasks into the CustomLinkedList
        }
    }

    // Undo last task action
    public void undo() {
        TaskHistoryManager.HistoryRecord<T> lastAction = historyManager.undo();
        if (lastAction != null) {
            if (lastAction.operation().equals("Add")) {
                removeTask(lastAction.task()); // Undo the add operation by removing the task
            } else if (lastAction.operation().equals("Remove")) {
                addTask(lastAction.task()); // Undo the remove operation by adding the task back
            }
        }
    }

    // Redo last undone task
    public void redo() {
        TaskHistoryManager.HistoryRecord<T> lastUndone = historyManager.redo();
        if (lastUndone != null) {
            if (lastUndone.operation().equals("Add")) {
                addTask(lastUndone.task()); // Redo the add operation by adding the task again
            } else if (lastUndone.operation().equals("Remove")) {
                removeTask(lastUndone.task()); // Redo the remove operation by removing the task again
            }
        }
    }
}
