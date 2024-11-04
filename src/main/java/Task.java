import java.io.Serializable;
import java.time.LocalDate;

public class Task implements Serializable {
    private final String title;
    private final String description;
    private final int priority;
    private final LocalDate dueDate;

    public Task(String title, String description, int priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    @Override
    public String toString() {
        return title + " - Priority: " + priority + " - Due: " + dueDate;
    }
}
