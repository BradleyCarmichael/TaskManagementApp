import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Task extends Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int priority;
    private final LocalDate dueDate;
    private final List<Task> dependencies;

    public Task(String title, String description, int priority, LocalDate dueDate) {
        super(title, description);
        this.priority = priority;
        this.dueDate = dueDate;
        this.dependencies = new ArrayList<>();
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public List<Task> getDependencies() {
        return dependencies;
    }

    public void addDependency(Task task) {
        dependencies.add(task);
    }

    @Override
    public String toString() {

        // Format the due date as MM/dd/yyyy to make it easier to rread
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String formattedDate = dueDate.format(formatter);

        return getTitle() + " - Priority: " + priority + " - Due: " + formattedDate;
    }
}
