import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private final String title;
    private final String description;
    private final int priority;
    private final LocalDate dueDate;

    // Constructor now has only four parameters
    public Task(String title, String description, int priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public String getDescription() { return description; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return title + " (Priority: " + priority + ", Due: " + dueDate.format(formatter) + ")";
    }
}
