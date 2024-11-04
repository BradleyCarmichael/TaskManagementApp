import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class MainApp extends Application {
    private final TaskManager taskManager = new TaskManager();
    private final ListView<Task> taskListView = new ListView<>();
    private final TextArea taskDescriptionArea = new TextArea(); // TextArea to display task description

    @Override
    public void start(Stage primaryStage) {
        // Input fields for tasks
        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Task Description");

        // Priority Spinner with infinite range
        Spinner<Integer> prioritySpinner = new Spinner<>();
        prioritySpinner.setEditable(true);
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        prioritySpinner.setValueFactory(valueFactory);
        prioritySpinner.getEditor().setPromptText("Enter Priority"); // Placeholder text

        DatePicker dueDatePicker = new DatePicker();

        // Button to add a task
        Button addTaskButton = new Button("Add Task");
        addTaskButton.setOnAction(_ -> {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            int priority = prioritySpinner.getValue();
            LocalDate dueDate = dueDatePicker.getValue();

            if (title.isEmpty() || dueDate == null) {
                showAlert("Error", "Title and Due Date are required.");
            } else {
                // Create a Task without formattedDate
                Task task = new Task(title, description, priority, dueDate);
                taskManager.addTask(task);
                updateTaskList();
                clearFields(titleField, descriptionArea, prioritySpinner, dueDatePicker);
            }
        });

        // Button to delete the selected task
        Button deleteTaskButton = getButton();

        // Button to save tasks to file
        Button saveButton = new Button("Save Tasks");
        saveButton.setOnAction(_ -> {
            try {
                taskManager.saveTasksToFile("tasks.dat");
                showAlert("Success", "Tasks saved successfully!");
            } catch (IOException ex) {
                showAlert("Error", "Failed to save tasks.");
            }
        });

        // Button to load tasks from file
        Button loadButton = new Button("Load Tasks");
        loadButton.setOnAction(_ -> {
            try {
                taskManager.loadTasksFromFile("tasks.dat");
                updateTaskList();
                showAlert("Success", "Tasks loaded successfully!");
            } catch (IOException | ClassNotFoundException ex) {
                showAlert("Error", "Failed to load tasks.");
            }
        });

        // Update task description area when a task is selected
        taskListView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                taskDescriptionArea.setText(newValue.getDescription());
            } else {
                taskDescriptionArea.clear();
            }
        });

        // Set up the layout and scene
        VBox layout = new VBox(10, titleField, descriptionArea, prioritySpinner, dueDatePicker, addTaskButton, deleteTaskButton, saveButton, loadButton, taskListView, taskDescriptionArea);
        taskDescriptionArea.setEditable(false); // Make the description area read-only
        taskDescriptionArea.setPromptText("Task Description will be displayed here");
        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Task Manager");
        primaryStage.show();
    }

    private Button getButton() {
        Button deleteTaskButton = new Button("Delete Task");
        deleteTaskButton.setOnAction(_ -> {
            Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                taskManager.getTasks().remove(selectedTask); // Remove from TaskManager
                updateTaskList(); // Update the ListView
                taskDescriptionArea.clear(); // Clear the description area
                showAlert("Success", "Task deleted successfully!");
            } else {
                showAlert("Error", "No task selected to delete.");
            }
        });
        return deleteTaskButton;
    }

    private void updateTaskList() {
        taskListView.getItems().setAll(taskManager.getTasks());
        // No need to format tasks for display since we're just using the toString method
    }

    private void clearFields(TextField titleField, TextArea descriptionArea, Spinner<Integer> prioritySpinner, DatePicker dueDatePicker) {
        titleField.clear();
        descriptionArea.clear();
        prioritySpinner.getValueFactory().setValue(1);
        dueDatePicker.setValue(null); // Clear due date
        taskDescriptionArea.clear(); // Clear the description area when fields are cleared
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
