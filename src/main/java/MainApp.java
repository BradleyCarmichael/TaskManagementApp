import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private final TaskManager<Task> taskManager = new TaskManager<>();
    private final ListView<Task> taskListView = new ListView<>();
    private final TextField titleField = new TextField();
    private final TextArea descriptionField = new TextArea();
    private final Spinner<Integer> prioritySpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);
    private final DatePicker dueDatePicker = new DatePicker(LocalDate.now());

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Task Manager");

        // UI Elements
        Label titleLabel = new Label("Title:");
        Label descriptionLabel = new Label("Description:");
        Label priorityLabel = new Label("Priority:");
        Label dueDateLabel = new Label("Due Date:");

        Button addTaskButton = new Button("Add Task");
        Button removeTaskButton = new Button("Remove Selected Task");
        Button undoButton = new Button("Undo");
        Button redoButton = new Button("Redo");
        Button saveButton = new Button("Save");
        Button loadButton = new Button("Load");

        // Create ComboBox for sorting options
        ComboBox<String> sortComboBox = new ComboBox<>();
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Sort by Priority", "Sort by Due Date"
        );
        sortComboBox.setItems(sortOptions);
        sortComboBox.setValue("Sort by Priority"); // Default value
        sortComboBox.setOnAction(_ -> handleSort(sortComboBox.getValue()));

        // Create search button with '?' symbol
        Button searchButton = new Button("⌕");
        searchButton.setTooltip(new Tooltip("Click to search tasks by name"));

        // Create clear search button
        Button clearSearchButton = new Button("X");
        clearSearchButton.setTooltip(new Tooltip("Click to clear the search filter"));

        // Event Handlers
        searchButton.setOnAction(e -> showSearchPrompt());
        clearSearchButton.setOnAction(e -> clearSearch());

        // Group related buttons in HBoxes
        HBox taskButtons = new HBox(10, addTaskButton, removeTaskButton);
        HBox undoRedoButtons = new HBox(10, undoButton, redoButton);
        HBox saveLoadButtons = new HBox(10, saveButton, loadButton);

        // Event Handlers for other buttons
        addTaskButton.setOnAction(_ -> addTask());
        removeTaskButton.setOnAction(_ -> removeSelectedTask());
        undoButton.setOnAction(_ -> undoLastTask());
        redoButton.setOnAction(_ -> redoLastTask());
        saveButton.setOnAction(_ -> saveTasksToFile());
        loadButton.setOnAction(_ -> loadTasksFromFile());

        // Set custom date format for the DatePicker
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        dueDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });

        // Initialize the priority spinner with a range and default value
        prioritySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));

        // Layout with padding and spacing
        VBox layout = new VBox(10,
                titleLabel, titleField,
                descriptionLabel, descriptionField,
                priorityLabel, prioritySpinner,
                dueDateLabel, dueDatePicker,
                new HBox(10, taskButtons),  // Task-related buttons (Add/Remove)
                new HBox(10, undoRedoButtons),  // Undo/Redo buttons
                new HBox(10, saveLoadButtons),  // Save/Load buttons
                sortComboBox,  // Only the ComboBox for sorting
                createTaskSearchHBox(searchButton, clearSearchButton),  // "Tasks:" label and buttons
                taskListView
        );


        // Add Padding (space) to the layout
        layout.setPadding(new Insets(10, 20, 10, 20));

        // Set scene with adjusted height
        Scene scene = new Scene(layout, 400, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize Task List
        updateTaskList();
    }

    // Method to handle sorting tasks by priority and due date
    private void handleSort(String sortOption) {
        if ("Sort by Priority".equals(sortOption)) {
            taskManager.sortTasksByPriority();
        } else if ("Sort by Due Date".equals(sortOption)) {
            taskManager.sortTasksByDueDate();
        }
        updateTaskList();
    }

    // Method to show a prompt when clicking the "?" button
    private void showSearchPrompt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Tasks by Name");
        dialog.setHeaderText("Enter a task name to search for:");
        dialog.setContentText("Task Name:");

        dialog.showAndWait().ifPresent(input -> {
            if (input != null && !input.isEmpty()) {
                filterTasksByName(input);
            }
        });
    }

    // Method to filter and sort tasks based on the name input
    private void filterTasksByName(String searchTerm) {
        List<Task> filteredTasks = taskManager.getTasks().stream()
                .filter(task -> task.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) // Case-insensitive search
                .sorted((task1, task2) -> task1.getTitle().compareToIgnoreCase(task2.getTitle())) // Sort alphabetically by task name
                .collect(Collectors.toList());

        // Update the task list view with the filtered and sorted tasks
        ObservableList<Task> tasks = FXCollections.observableArrayList(filteredTasks);
        taskListView.setItems(tasks);
    }

    // Method to clear the search filter and reset the task list to original
    private void clearSearch() {
        updateTaskList(); // Reset to show all tasks
    }

    // Method to add a new task
    private void addTask() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        int priority = prioritySpinner.getValue();
        LocalDate dueDate = dueDatePicker.getValue();

        // Create a new task with the title, description, priority, and due date
        Task newTask = new Task(title, description, priority, dueDate);
        taskManager.addTask(newTask);
        updateTaskList();

        // Clear input fields after adding a task
        titleField.clear();
        descriptionField.clear();

        // Reset the priority spinner value back to 1
        prioritySpinner.getValueFactory().setValue(1);

        // Reset due date to today's date after adding the task
        dueDatePicker.setValue(LocalDate.now());
    }

    // Method to remove the selected task
    private void removeSelectedTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            taskManager.removeTask(selectedTask);
            updateTaskList();
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to remove.");
        }
    }

    // Method to undo the last task operation
    private void undoLastTask() {
        taskManager.undo();
        updateTaskList();
    }

    // Method to redo the last undone task operation
    private void redoLastTask() {
        taskManager.redo();
        updateTaskList();
    }

    // Method to update the task list in the UI
    private void updateTaskList() {
        ObservableList<Task> tasks = FXCollections.observableArrayList(taskManager.getTasks());
        taskListView.setItems(tasks);
    }

    // Method to save tasks to a file
    private void saveTasksToFile() {
        try {
            taskManager.saveTasksToFile("tasks.dat");
            showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Tasks have been saved.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Error saving tasks: " + e.getMessage());
        }
    }

    // Method to load tasks from a file
    private void loadTasksFromFile() {
        try {
            taskManager.loadTasksFromFile("tasks.dat");
            updateTaskList();
            showAlert(Alert.AlertType.INFORMATION, "Load Successful", "Tasks have been loaded.");
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Error loading tasks: " + e.getMessage());
        }
    }

    // Method to show alert dialogs
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to create the HBox with right-aligned buttons
    private HBox createTaskSearchHBox(Button searchButton, Button clearSearchButton) {
        // Create a Region to push the buttons to the far right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox taskSearchHBox = new HBox(10, new Label("Tasks:"), spacer, searchButton, clearSearchButton);

        return taskSearchHBox;
    }



    public static void main(String[] args) {
        launch(args);
    }
}