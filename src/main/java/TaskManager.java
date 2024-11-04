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

    /**
     * Sorts tasks by priority using Merge Sort.
     * Merge Sort has an average and worst-case time complexity of O(n log n),
     * making it efficient for large datasets.
     */
    public void sortByPriority() {
        if (tasks.isEmpty()) return;
        tasks = mergeSort(tasks, Comparator.comparingInt(task -> ((Task) task).getPriority()));
    }

    /**
     * Sorts tasks by due date using Merge Sort.
     * Merge Sort is stable and efficient for datasets that need to maintain relative order.
     */
    public void sortByDueDate() {
        if (tasks.isEmpty()) return;
        tasks = mergeSort(tasks, Comparator.comparing(task -> ((Task) task).getDueDate()));
    }

    /**
     * Generic Merge Sort method for sorting an ArrayList of items based on a given comparator.
     *
     * @param list       The list to sort.
     * @param comparator The comparator defining the sorting criteria.
     * @return A sorted ArrayList.
     */
    private ArrayList<T> mergeSort(ArrayList<T> list, Comparator<T> comparator) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        ArrayList<T> left = new ArrayList<>(list.subList(0, mid));
        ArrayList<T> right = new ArrayList<>(list.subList(mid, list.size()));

        // Recursively sort each half
        left = mergeSort(left, comparator);
        right = mergeSort(right, comparator);

        // Merge the sorted halves
        return merge(left, right, comparator);
    }

    /**
     * Merges two sorted ArrayLists into a single sorted ArrayList.
     *
     * @param left       The left sorted ArrayList.
     * @param right      The right sorted ArrayList.
     * @param comparator The comparator defining the sorting criteria.
     * @return A merged, sorted ArrayList.
     */

    private ArrayList<T> merge(ArrayList<T> left, ArrayList<T> right, Comparator<T> comparator) {
        ArrayList<T> mergedList = new ArrayList<>();
        int leftIndex = 0, rightIndex = 0;

        // Compare elements from left and right lists, adding the smaller element to the merged list
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (comparator.compare(left.get(leftIndex), right.get(rightIndex)) <= 0) {
                mergedList.add(left.get(leftIndex++));
            } else {
                mergedList.add(right.get(rightIndex++));
            }
        }

        // Append any remaining elements from the left list
        while (leftIndex < left.size()) {
            mergedList.add(left.get(leftIndex++));
        }

        // Append any remaining elements from the right list
        while (rightIndex < right.size()) {
            mergedList.add(right.get(rightIndex++));
        }

        return mergedList;
    }
}
