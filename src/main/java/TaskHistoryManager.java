import java.util.*;

public class TaskHistoryManager<T> {
    private final Stack<HistoryRecord<T>> undoStack = new Stack<>();
    private final Stack<HistoryRecord<T>> redoStack = new Stack<>();

    // Create HistoryRecord class
        public record HistoryRecord<T>(T task, String operation) {
    }

    public void addToHistory(T task, String operation) {
        HistoryRecord<T> record = new HistoryRecord<>(task, operation);
        undoStack.push(record);
        redoStack.clear(); // Clear redo stack when new action is added
    }

    public HistoryRecord<T> undo() {
        if (!undoStack.isEmpty()) {
            HistoryRecord<T> lastAction = undoStack.pop();
            redoStack.push(lastAction);
            return lastAction;
        }
        return null;
    }

    public HistoryRecord<T> redo() {
        if (!redoStack.isEmpty()) {
            HistoryRecord<T> lastUndone = redoStack.pop();
            undoStack.push(lastUndone);
            return lastUndone;
        }
        return null;
    }

}
