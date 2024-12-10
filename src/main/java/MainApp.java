import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

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

        // Create ComboBox for bulk delete options
        ComboBox<String> bulkDeleteComboBox = new ComboBox<>();
        ObservableList<String> bulkDeleteOptionsList = FXCollections.observableArrayList(
                "Delete by Name", "Delete by Priority"
        );
        bulkDeleteComboBox.setItems(bulkDeleteOptionsList);
        bulkDeleteComboBox.setValue("Bulk Delete");

        bulkDeleteComboBox.setOnAction(e -> handleBulkDeleteOption(bulkDeleteComboBox.getValue()));

        // Create ComboBox for sorting options
        ComboBox<String> sortComboBox = new ComboBox<>();
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Sort by Priority", "Sort by Due Date"
        );
        sortComboBox.setItems(sortOptions);
        sortComboBox.setValue("Sort by Priority");
        sortComboBox.setOnAction(_ -> handleSort(sortComboBox.getValue()));

        // Create search button with '?' symbol
        Button searchButton = new Button("⌕");
        searchButton.setTooltip(new Tooltip("Click to search tasks by name"));

        // Create clear search button
        Button clearSearchButton = new Button("X");
        clearSearchButton.setTooltip(new Tooltip("Click to clear the search filter"));

        // Event Handlers
        addTaskButton.setOnAction(_ -> addTask());
        removeTaskButton.setOnAction(_ -> removeSelectedTask());
        undoButton.setOnAction(_ -> undoLastTask());
        redoButton.setOnAction(_ -> redoLastTask());
        saveButton.setOnAction(_ -> saveTasksToFile());
        loadButton.setOnAction(_ -> loadTasksFromFile());
        searchButton.setOnAction(e -> showSearchPrompt());
        clearSearchButton.setOnAction(e -> clearSearch());

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

        prioritySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));

        // Layout with padding and spacing
        HBox taskButtons = new HBox(10, addTaskButton, removeTaskButton, bulkDeleteComboBox);
        taskButtons.setSpacing(10);
        taskButtons.setHgrow(bulkDeleteComboBox, Priority.ALWAYS);  // Align bulk delete combo box to the right

        VBox layout = new VBox(10,
                titleLabel, titleField,
                descriptionLabel, descriptionField,
                priorityLabel, prioritySpinner,
                dueDateLabel, dueDatePicker,
                taskButtons,  // Updated task-related buttons (Add/Remove/Bulk Delete)
                new HBox(10, undoButton, redoButton),
                new HBox(10, saveButton, loadButton),
                sortComboBox,
                createTaskSearchHBox(searchButton, clearSearchButton),
                taskListView
        );
        layout.setPadding(new Insets(10, 20, 10, 20));

        Scene scene = new Scene(layout, 400, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateTaskList();
    }

    private void handleSort(String sortOption) {
        if ("Sort by Priority".equals(sortOption)) {
            taskManager.sortTasksByPriority();
        } else if ("Sort by Due Date".equals(sortOption)) {
            taskManager.sortTasksByDueDate();
        }
        updateTaskList();
    }

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

    private void filterTasksByName(String searchTerm) {
        List<Task> filteredTasks = taskManager.getTasks().stream()
                .filter(task -> task.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) // Case-insensitive search
                .sorted((task1, task2) -> task1.getTitle().compareToIgnoreCase(task2.getTitle())) // Sort alphabetically by task name
                .collect(Collectors.toList());

        // Update the task list view with the filtered and sorted tasks
        ObservableList<Task> tasks = FXCollections.observableArrayList(filteredTasks);
        taskListView.setItems(tasks);
    }

    private void clearSearch() {
        updateTaskList();
    }

    private void addTask() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        int priority = prioritySpinner.getValue();
        LocalDate dueDate = dueDatePicker.getValue();

        Task newTask = new Task(title, description, priority, dueDate);
        taskManager.addTask(newTask);
        updateTaskList();

        titleField.clear();
        descriptionField.clear();
        prioritySpinner.getValueFactory().setValue(1);
        dueDatePicker.setValue(LocalDate.now());
    }

    private void removeSelectedTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            taskManager.removeTask(selectedTask);
            updateTaskList();
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to remove.");
        }
    }

    private void undoLastTask() {
        taskManager.undo();
        updateTaskList();
    }

    private void redoLastTask() {
        taskManager.redo();
        updateTaskList();
    }

    private void deleteTasksByName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete by Name");
        dialog.setHeaderText("Delete Tasks by Name");
        dialog.setContentText("Enter the name or part of the name:");
        dialog.showAndWait().ifPresent(name -> {
            BST bst = new BST();  // Create the BST for bulk deletion

            // Insert tasks to be deleted into the BST
            taskManager.getTasks().stream()
                    .filter(task -> task.getTitle().contains(name))
                    .forEach(task -> bst.insert(task.getTitle()));

            // Find and remove tasks that match the name criteria
            List<Task> tasksToRemove = taskManager.getTasks().stream()
                    .filter(task -> bst.contains(task.getTitle()))  // Check if task is in BST
                    .collect(Collectors.toList());

            // Remove the tasks from taskManager
            tasksToRemove.forEach(taskManager::removeTask);
            updateTaskList();
            showAlert(Alert.AlertType.INFORMATION, "Tasks Deleted", tasksToRemove.size() + " tasks deleted.");
        });
    }

    private void deleteTasksByPriority() {
        TextInputDialog startDialog = new TextInputDialog();
        startDialog.setTitle("Delete by Priority");
        startDialog.setHeaderText("Delete Tasks by Priority Range");
        startDialog.setContentText("Enter the starting priority:");
        startDialog.showAndWait().ifPresent(startPriorityStr -> {
            TextInputDialog endDialog = new TextInputDialog();
            endDialog.setTitle("Delete by Priority");
            endDialog.setHeaderText("Delete Tasks by Priority Range");
            endDialog.setContentText("Enter the ending priority:");
            endDialog.showAndWait().ifPresent(endPriorityStr -> {
                try {
                    int startPriority = Integer.parseInt(startPriorityStr);
                    int endPriority = Integer.parseInt(endPriorityStr);
                    if (startPriority > endPriority) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Range", "Start priority must be less than or equal to end priority.");
                        return;
                    }

                    // Create a BST for task priorities
                    BST bst = new BST();
                    taskManager.getTasks().stream()
                            .filter(task -> task.getPriority() >= startPriority && task.getPriority() <= endPriority)
                            .forEach(task -> bst.insert(String.valueOf(task.getPriority())));  // Insert priority as string

                    // Find and remove tasks that match the priority range
                    List<Task> tasksToRemove = taskManager.getTasks().stream()
                            .filter(task -> bst.contains(String.valueOf(task.getPriority())))  // Check priority in BST
                            .collect(Collectors.toList());

                    // Remove the tasks from taskManager
                    tasksToRemove.forEach(taskManager::removeTask);
                    updateTaskList();
                    showAlert(Alert.AlertType.INFORMATION, "Tasks Deleted", tasksToRemove.size() + " tasks deleted.");
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Priority values must be numbers.");
                }
            });
        });
    }

    private void handleBulkDeleteOption(String selectedOption) {
        switch (selectedOption) {
            case "Delete by Name":
                deleteTasksByName();
                break;
            case "Delete by Priority":
                deleteTasksByPriority();
                break;
            default:
                break;
        }
    }

    private void updateTaskList() {
        taskListView.setItems(FXCollections.observableArrayList(taskManager.getTasks()));
    }

    private void saveTasksToFile() {
        try {
            taskManager.saveTasksToFile("tasks.dat");
            showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Tasks have been saved.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Error saving tasks: " + e.getMessage());
        }
    }

    private void loadTasksFromFile() {
        try {
            taskManager.loadTasksFromFile("tasks.dat");
            updateTaskList();
            showAlert(Alert.AlertType.INFORMATION, "Load Successful", "Tasks have been loaded.");
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Error loading tasks: " + e.getMessage());
        }
    }

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
