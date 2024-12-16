import java.util.*;

// Keeps track of changes to tasks so we can undo and redo them
public class TaskHistoryManager<T> {
    // Two stacks: one for undo, one for redo
    private final Stack<HistoryRecord<T>> undoStack = new Stack<>();
    private final Stack<HistoryRecord<T>> redoStack = new Stack<>();

    // Stores what happened to a task
    public record HistoryRecord<T>(T task, String operation) {
    }

    // Saves a task change to history
    // Clears redo stack since we're adding a new change
    public void addToHistory(T task, String operation) {
        HistoryRecord<T> record = new HistoryRecord<>(task, operation);
        undoStack.push(record);
        redoStack.clear();
    }

    // Takes back the last change made
    // Returns what was undone, or null if nothing to undo
    public HistoryRecord<T> undo() {
        if (!undoStack.isEmpty()) {
            HistoryRecord<T> lastAction = undoStack.pop();
            redoStack.push(lastAction);
            return lastAction;
        }
        return null;
    }

    // Brings back the last change that was undone
    // Returns what was redone, or null if nothing to redo
    public HistoryRecord<T> redo() {
        if (!redoStack.isEmpty()) {
            HistoryRecord<T> lastUndone = redoStack.pop();
            undoStack.push(lastUndone);
            return lastUndone;
        }
        return null;
    }
}