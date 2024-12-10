import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Task extends Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int priority;
    private final LocalDate dueDate;

    public Task(String title, String description, int priority, LocalDate dueDate) {
        super(title);
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    @Override
    public String toString() {

        // Format the due date as MM/dd/yyyy to make it easier to read
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String formattedDate = dueDate.format(formatter);

        return getTitle() + " - Priority: " + priority + " - Due: " + formattedDate;
    }
}
