import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

// Handles background tasks and multi-threading operations
// Uses a thread pool to run tasks without freezing the UI
public class TaskExecutor {
    private final ExecutorService executorService;
    private final ThreadPoolExecutor threadPool;
    // Keeps track of how many threads are currently running
    private final IntegerProperty activeThreads = new SimpleIntegerProperty(0);

    public TaskExecutor() {
        // Create a thread pool that adjusts to the computer's capabilities
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
        this.executorService = threadPool;
        
        // Start watching how many threads are being used
        startThreadMonitor();
    }

    // Watches and updates the number of active threads
    private void startThreadMonitor() {
        Thread monitor = new Thread(() -> {
            while (!threadPool.isShutdown()) {
                activeThreads.set(threadPool.getActiveCount());
                try {
                    Thread.sleep(100); // Check every 100ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    // Gets the maximum number of threads available
    public int getMaxThreads() {
        return threadPool.getCorePoolSize();
    }

    // Used by the UI to show how many threads are running
    public IntegerProperty activeThreadsProperty() {
        return activeThreads;
    }

    // Saves tasks in the background
    public CompletableFuture<Void> saveTasksAsync(TaskManager<?> taskManager, String filename) {
        return CompletableFuture.runAsync(() -> {
            try {
                taskManager.saveTasksToFile(filename);
            } catch (Exception e) {
                throw new RuntimeException("Error saving tasks: " + e.getMessage(), e);
            }
        }, executorService);
    }

    // Loads tasks in the background
    public CompletableFuture<Void> loadTasksAsync(TaskManager<?> taskManager, String filename) {
        return CompletableFuture.runAsync(() -> {
            try {
                taskManager.loadTasksFromFile(filename);
            } catch (Exception e) {
                throw new RuntimeException("Error loading tasks: " + e.getMessage(), e);
            }
        }, executorService);
    }

    // Deletes multiple tasks in the background
    public CompletableFuture<Void> bulkDeleteAsync(List<Task> tasks, TaskManager<Task> taskManager) {
        return CompletableFuture.runAsync(() -> {
            tasks.forEach(taskManager::removeTask);
        }, executorService);
    }

    // Cleans up threads when the program closes
    public void shutdown() {
        executorService.shutdown();
    }
} 