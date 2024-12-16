import java.io.Serial;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Represents a task with a title, description, priority level, and due date.
// Extends the basic Item class to add more features.
public class Task extends Item {
    @Serial
    private static final long serialVersionUID = 1L;

    // Basic information about the task
    private final int priority;
    private final LocalDate dueDate;
    private final String description;

    // Creates a new task with all its details
    public Task(String title, String description, int priority, LocalDate dueDate) {
        super(title);
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    // Simple methods to get task information
    public int getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    // Converts the task to a readable string format
    // Shows the title, priority, and due date
    @Override
    public String toString() {
        // Make the date look nice (MM/dd/yyyy format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String formattedDate = dueDate.format(formatter);

        return getTitle() + " - Priority: " + priority + " - Due: " + formattedDate;
    }
}
