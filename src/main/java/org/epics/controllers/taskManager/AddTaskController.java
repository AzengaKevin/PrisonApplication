package org.epics.controllers.taskManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.epics.data.Datasource;
import org.epics.data.entities.Task;
import org.epics.data.repositories.TaskRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddTaskController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker startDateField;
    @FXML
    private DatePicker endDateField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;

    private Datasource datasource;
    private Executor executor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        datasource = Datasource.getInstance();

        executor = Executors.newCachedThreadPool(r -> {

            Thread thread = new Thread(r);
            thread.setDaemon(true);

            return thread;
        });


        cancelButton.setOnAction(event -> closeStage());

        submitButton.setOnAction(event -> handAddTaskEvent());
    }

    private void handAddTaskEvent() {

        String title = titleField.getText();
        String description = descriptionField.getText();
        String startDateText = startDateField.getEditor().getText();
        String endDateText = endDateField.getEditor().getText();

        if (title.isEmpty() || description.isEmpty() || startDateText.isEmpty() || endDateText.isEmpty()) {
            AlertHelper.showErrorAlert("Adding Task", "All the fields are required");
            return;
        }


        LocalDate startLocalDate = startDateField.getValue();
        Instant startInstant = Instant.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()));
        Date startDate = Date.from(startInstant);

        LocalDate endLocalDate = endDateField.getValue();
        Instant endInstant = Instant.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()));
        Date endDate = Date.from(endInstant);

        Task task = new Task(title, description, startDate, endDate);

        javafx.concurrent.Task<Task> addTaskTask = new javafx.concurrent.Task<Task>() {
            @Override
            protected Task call() throws Exception {

                TaskRepository taskRepository = new TaskRepository(datasource.getEntityManager());
                return taskRepository.save(task).orElseThrow(IllegalAccessError::new);
            }
        };

        addTaskTask.setOnFailed(workerStateEvent -> {

            Log.error(getClass().getSimpleName(), "handAddTaskEvent: failed", workerStateEvent.getSource().getException());
            AlertHelper.showErrorAlert("Adding Task Operation", "The opertation failed, check the logs fro more intel");

        });

        addTaskTask.setOnSucceeded(workerStateEvent -> {
            AlertHelper.showInformationAlert("Add Task Operation", "New Task Successfully Added");
            closeStage();
        });

        executor.execute(addTaskTask);
    }

    public void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        //stage.getOnCloseRequest().notify();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
