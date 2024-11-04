import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
        prioritySpinner.getEditor().setPromptText("Enter Priority");

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
                Task task = new Task(title, description, priority, dueDate);
                taskManager.addTask(task);
                updateTaskList();
                clearFields(titleField, descriptionArea, prioritySpinner, dueDatePicker);
            }
        });

        // Button to delete the selected task
        Button deleteTaskButton = new Button("Delete Task");
        deleteTaskButton.setOnAction(_ -> {
            Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                taskManager.getTasks().remove(selectedTask);
                updateTaskList();
                taskDescriptionArea.clear();
                showAlert("Success", "Task deleted successfully!");
            } else {
                showAlert("Error", "No task selected to delete.");
            }
        });

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

        // Sort ComboBox
        ComboBox<String> sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Sort by Priority", "Sort by Due Date");
        sortComboBox.setPromptText("Sort Tasks");

        // Sort based on ComboBox selection
        sortComboBox.setOnAction(_ -> {
            String selectedSort = sortComboBox.getValue();
            if ("Sort by Priority".equals(selectedSort)) {
                taskManager.sortByPriority();
            } else if ("Sort by Due Date".equals(selectedSort)) {
                taskManager.sortByDueDate();
            }
            updateTaskList();
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
        HBox topLayout = new HBox(10, addTaskButton, sortComboBox);
        VBox layout = new VBox(10, titleField, descriptionArea, prioritySpinner, dueDatePicker, topLayout, deleteTaskButton, saveButton, loadButton, taskListView, taskDescriptionArea);
        taskDescriptionArea.setEditable(false);
        taskDescriptionArea.setPromptText("Task Description will be displayed here");

        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Task Manager");
        primaryStage.show();
    }

    private void updateTaskList() {
        taskListView.getItems().setAll(taskManager.getTasks());
    }

    private void clearFields(TextField titleField, TextArea descriptionArea, Spinner<Integer> prioritySpinner, DatePicker dueDatePicker) {
        titleField.clear();
        descriptionArea.clear();
        prioritySpinner.getValueFactory().setValue(1);
        dueDatePicker.setValue(null);
        taskDescriptionArea.clear();
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
