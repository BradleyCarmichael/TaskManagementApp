import java.io.*;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

// Manages all tasks and their operations
// Handles adding, removing, sorting, and storing tasks
public class TaskManager<T extends Task> {
    private CustomLinkedList<T> tasks;
    private final TaskHistoryManager<T> historyManager;
    private final HashTable<String, T> taskTable;  // Stores tasks by title for quick lookup
    private final TaskCache<String, T> taskCache = new TaskCache<>(50); // Caches recent tasks

    // Sets up a new task manager with empty lists and storage
    public TaskManager() {
        tasks = new CustomLinkedList<>();
        historyManager = new TaskHistoryManager<>();
        taskTable = new HashTable<>();
    }

    // Adds a new task and updates all storage locations
    public void addTask(T task) {
        tasks.add(task);
        taskTable.put(task.getTitle(), task);
        taskCache.put(task.getTitle(), task);
        historyManager.addToHistory(task, "Add");
    }

    // Removes a task from all storage locations
    public void removeTask(T task) {
        int index = findTaskIndex(task);
        if (index != -1) {
            tasks.remove(index);
            taskTable.remove(task.getTitle());
            taskCache.clear();
            historyManager.addToHistory(task, "Remove");
        }
    }

    // Returns a list of all tasks
    public List<T> getTasks() {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            result.add(tasks.get(i));
        }
        return result;
    }

    // Sorts tasks by priority using quicksort
    public void sortTasksByPriority() {
        List<T> taskList = getTasks();
        quicksort(taskList, 0, taskList.size() - 1, Comparator.comparingInt(Task::getPriority));
        reloadTasksFromList(taskList);
    }

    // Sorts tasks by due date using quicksort
    public void sortTasksByDueDate() {
        List<T> taskList = getTasks();
        quicksort(taskList, 0, taskList.size() - 1, Comparator.comparing(Task::getDueDate));
        reloadTasksFromList(taskList);
    }

    // Quicksort implementation for sorting tasks
    private void quicksort(List<T> list, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, comparator);
            quicksort(list, low, pivotIndex - 1, comparator);
            quicksort(list, pivotIndex + 1, high, comparator);
        }
    }

    // Helper method for quicksort
    private int partition(List<T> list, int low, int high, Comparator<T> comparator) {
        T pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                T temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        T temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);

        return i + 1;
    }

    // Finds where a task is in the list
    private int findTaskIndex(T task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).equals(task)) {
                return i;
            }
        }
        return -1;
    }

    // Updates storage after sorting or loading tasks
    private void reloadTasksFromList(List<T> taskList) {
        tasks = new CustomLinkedList<>();
        for (T task : taskList) {
            tasks.add(task);
            taskTable.put(task.getTitle(), task);
        }
    }

    // Saves all tasks to a file
    public void saveTasksToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(getTasks());
        }
    }

    // Loads tasks from a file
    @SuppressWarnings("unchecked")
    public void loadTasksFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            List<T> loadedTasks = (List<T>) in.readObject();
            reloadTasksFromList(loadedTasks);
        }
    }

    // Undoes the last action (add or remove)
    public void undo() {
        TaskHistoryManager.HistoryRecord<T> lastAction = historyManager.undo();
        if (lastAction != null) {
            if (lastAction.operation().equals("Add")) {
                removeTask(lastAction.task());
            } else if (lastAction.operation().equals("Remove")) {
                addTask(lastAction.task());
            }
        }
    }

    // Redoes the last undone action
    public void redo() {
        TaskHistoryManager.HistoryRecord<T> lastUndone = historyManager.redo();
        if (lastUndone != null) {
            if (lastUndone.operation().equals("Add")) {
                addTask(lastUndone.task());
            } else if (lastUndone.operation().equals("Remove")) {
                removeTask(lastUndone.task());
            }
        }
    }

    // Gets a task by its title, checking cache first
    public T getTaskByTitle(String title) {
        T cachedTask = taskCache.get(title);
        if (cachedTask != null) {
            return cachedTask;
        }
        
        T task = taskTable.get(title);
        if (task != null) {
            taskCache.put(title, task);
        }
        return task;
    }
}
