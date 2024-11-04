import java.time.LocalDate;

public class Task extends Item {
    private final int priority;
    private final LocalDate dueDate;

    public Task(String title, String description, int priority, LocalDate dueDate) {
        super(title, description);
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
        return getTitle() + " - Priority: " + priority + " - Due: " + dueDate;
    }
}
