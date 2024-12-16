import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;

public class MainApp extends Application {
    // Stores and manages all our tasks
    private final TaskManager<Task> taskManager = new TaskManager<>();
    
    // Main list that shows all tasks
    private final ListView<Task> taskListView = new ListView<>();
    
    // Fields for adding new tasks
    private final TextField titleField = new TextField();
    private final TextArea descriptionField = new TextArea();
    private final Spinner<Integer> prioritySpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);
    private final DatePicker dueDatePicker = new DatePicker(LocalDate.now());
    
    // Handles background operations
    private final TaskExecutor taskExecutor = new TaskExecutor();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Label threadInfoLabel = new Label();

    // Colors for different priority levels
    private static final String HIGH_PRIORITY_STYLE = "-fx-background-color: #c8e6c9;"; // Light green
    private static final String MEDIUM_PRIORITY_STYLE = "#fff9c4;"; // Light yellow
    private static final String LOW_PRIORITY_STYLE = "#ffcdd2;"; // Light red

    // Sets up the main window and all its parts
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

        bulkDeleteComboBox.setOnAction(_ -> handleBulkDeleteOption(bulkDeleteComboBox.getValue()));

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
        searchButton.setOnAction(_ -> showSearchPrompt());
        clearSearchButton.setOnAction(_ -> clearSearch());

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
        HBox.setHgrow(bulkDeleteComboBox, Priority.ALWAYS);  // Aligns the bulk delete combo box to the right

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
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20, 20);

        HBox statusBar = new HBox(10, threadInfoLabel, progressIndicator);
        statusBar.setAlignment(Pos.CENTER_RIGHT);

        layout.getChildren().add(statusBar);

        taskExecutor.activeThreadsProperty().addListener((_, _, newVal) -> {
            Platform.runLater(() -> {
                threadInfoLabel.setText(String.format("Active Threads: %d/%d", 
                    newVal.intValue(), 
                    taskExecutor.getMaxThreads()));
            });
        });

        taskListView.setCellFactory(_ -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(task.toString());
                    setStyle(getPriorityStyle(task));
                    
                    // Add warning icon for tasks due within 3 days
                    if (task.getDueDate().isBefore(LocalDate.now().plusDays(3))) {
                        setText("⚠ " + task.toString());
                    }
                }
            }
        });

        taskListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {
                    showTaskDescriptionWindow(selectedTask);
                }
            }
        });
    }

    // Adds a new task using the input fields
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

    // Removes the currently selected task
    private void removeSelectedTask() {
        Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            taskManager.removeTask(selectedTask);
            updateTaskList();
        } else {
            showAlert(Alert.AlertType.WARNING, "No Task Selected", "Please select a task to remove.");
        }
    }

    // Undoes the last action (add or remove task)
    private void undoLastTask() {
        taskManager.undo();
        updateTaskList();
    }

    // Redoes the last undone action
    private void redoLastTask() {
        taskManager.redo();
        updateTaskList();
    }

    // Deletes tasks by matching their names
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
                    .toList();

            // Remove the tasks from taskManager
            tasksToRemove.forEach(taskManager::removeTask);
            updateTaskList();
            showAlert(Alert.AlertType.INFORMATION, "Tasks Deleted", tasksToRemove.size() + " tasks deleted.");
        });
    }

    // Deletes tasks within a priority range
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

                    progressIndicator.setVisible(true);
                    List<Task> tasksToRemove = taskManager.getTasks().stream()
                        .filter(task -> task.getPriority() >= startPriority && 
                                       task.getPriority() <= endPriority)
                        .toList();

                    taskExecutor.bulkDeleteAsync(tasksToRemove, taskManager)
                        .thenRunAsync(() -> {
                            updateTaskList();
                            progressIndicator.setVisible(false);
                            showAlert(Alert.AlertType.INFORMATION, "Tasks Deleted", 
                                tasksToRemove.size() + " tasks deleted.");
                        }, Platform::runLater)
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                progressIndicator.setVisible(false);
                                showAlert(Alert.AlertType.ERROR, "Delete Error", 
                                    "Error deleting tasks: " + throwable.getMessage());
                            });
                            return null;
                        });
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Priority values must be numbers.");
                }
            });
        });
    }

    // Shows a popup to confirm task deletion
    private void showDeleteConfirmation(List<Task> tasksToDelete) {
        // Implementation for showing delete confirmation
    }

    // Updates the thread count display
    private void updateThreadCount(Number newCount) {
        // Implementation for updating thread count
    }

    // Shows a window to search for tasks
    private void showSearchPrompt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Tasks by Name");
        dialog.setHeaderText("Enter a task name to search for:");
        dialog.setContentText("Task Name:");

        dialog.showAndWait().ifPresent(input -> {
            if (!input.isEmpty()) {
                filterTasksByName(input);
            }
        });
    }

    // Filters the task list to show only matching tasks
    private void filterTasksByName(String searchTerm) {
        List<Task> filteredTasks = taskManager.getTasks().stream()
                .filter(task -> task.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) // Case-insensitive search
                .sorted((task1, task2) -> task1.getTitle().compareToIgnoreCase(task2.getTitle())) // Sort alphabetically by task name
                .collect(Collectors.toList());

        // Update the task list view with the filtered and sorted tasks
        ObservableList<Task> tasks = FXCollections.observableArrayList(filteredTasks);
        taskListView.setItems(tasks);
    }

    // Removes any search filters and shows all tasks
    private void clearSearch() {
        updateTaskList();
    }

    // Handles which bulk delete option was chosen
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

    // Refreshes the list of tasks shown to the user
    private void updateTaskList() {
        taskListView.setItems(FXCollections.observableArrayList(taskManager.getTasks()));
    }

    // Saves all tasks to a file in the background
    private void saveTasksToFile() {
        progressIndicator.setVisible(true);
        taskExecutor.saveTasksAsync(taskManager, "tasks.dat")
            .thenRunAsync(() -> {
                progressIndicator.setVisible(false);
                showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Tasks have been saved.");
            }, Platform::runLater)
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Save Error", 
                        "Error saving tasks: " + throwable.getMessage());
                });
                return null;
            });
    }

    // Loads tasks from a file in the background
    private void loadTasksFromFile() {
        progressIndicator.setVisible(true);
        taskExecutor.loadTasksAsync(taskManager, "tasks.dat")
            .thenRunAsync(() -> {
                updateTaskList();
                progressIndicator.setVisible(false);
                showAlert(Alert.AlertType.INFORMATION, "Load Successful", "Tasks have been loaded.");
            }, Platform::runLater)
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Load Error", 
                        "Error loading tasks: " + throwable.getMessage());
                });
                return null;
            });
    }

    // Shows a popup message to the user
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Creates a search box with buttons aligned to the right
    private HBox createTaskSearchHBox(Button searchButton, Button clearSearchButton) {

        // Create a Region to push the buttons to the far right

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        return new HBox(10, new Label("Tasks:"), spacer, searchButton, clearSearchButton);
    }

    // Cleans up resources when the app closes
    @Override
    public void stop() {
        taskExecutor.shutdown();
    }

    // Starts the application
    public static void main(String[] args) {
        launch(args);
    }

    // Gets the color style based on task priority
    private String getPriorityStyle(Task task) {
        int priority = task.getPriority();
        if (priority >= 7) {
            return HIGH_PRIORITY_STYLE;
        } else if (priority >= 4) {
            return "-fx-background-color: " + MEDIUM_PRIORITY_STYLE;
        } else {
            return "-fx-background-color: " + LOW_PRIORITY_STYLE;
        }
    }

    // Shows a popup window with the task's full description
    private void showTaskDescriptionWindow(Task task) {
        Stage descriptionStage = new Stage();
        descriptionStage.initModality(Modality.APPLICATION_MODAL); // Makes the window modal
        descriptionStage.setTitle(task.getTitle());

        TextArea descArea = new TextArea(task.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(
            new Label("Description:"),
            descArea
        );

        Scene scene = new Scene(layout, 300, 200);
        descriptionStage.setScene(scene);
        descriptionStage.show();
    }

    // Sorts tasks by priority or due date
    private void handleSort(String sortOption) {
        if ("Sort by Priority".equals(sortOption)) {
            taskManager.sortTasksByPriority();
        } else if ("Sort by Due Date".equals(sortOption)) {
            taskManager.sortTasksByDueDate();
        }
        updateTaskList();
    }
}
