package org.epics.controllers.taskManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.epics.data.Datasource;
import org.epics.data.entities.InmateEntity;
import org.epics.data.entities.Task;
import org.epics.data.repositories.InmateRepository;
import org.epics.data.repositories.TaskRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AssignTaskController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private ComboBox<String> tasksField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;

    private Datasource datasource;
    private Executor executor;

    private InmateEntity inmateEntity;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        datasource = Datasource.getInstance();
        submitButton.setDisable(true);

        executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        cancelButton.setOnAction(event -> closeStage());
        submitButton.setOnAction(event -> handleAssignTaskToInmate());

        setupTasksField();

    }

    private void setupTasksField() {

        javafx.concurrent.Task<List<Task>> retrieveTasksTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<org.epics.data.entities.Task> call() throws Exception {
                TaskRepository taskRepository = new TaskRepository(datasource.getEntityManager());
                return taskRepository.findAll();
            }
        };

        retrieveTasksTask.setOnFailed(workerStateEvent -> {
            Log.error(getClass().getSimpleName(), "retrieveAndShowAllTasks: failed", workerStateEvent.getSource().getException());
            AlertHelper.showErrorAlert("Retrieving All Tasks", "The retrieval failed, check the logs for more information");
        });

        retrieveTasksTask.setOnSucceeded(workerStateEvent -> {

            List<Task> taskList = (List<Task>) workerStateEvent.getSource().getValue();

            List<String> fieldTaskList = taskList.stream().map(Task::getTitle).toList();

            tasksField.setItems(FXCollections.observableArrayList(fieldTaskList));

        });

        executor.execute(retrieveTasksTask);
    }

    private void handleAssignTaskToInmate() {
        String taskTitle = tasksField.getSelectionModel().getSelectedItem();

        if (taskTitle.isEmpty()) {
            AlertHelper.showErrorAlert("Assigning Task", "Please select a task to assign");
            return;
        }


        javafx.concurrent.Task<Task> retrieveTaskTask = new javafx.concurrent.Task<>() {

            @Override
            protected Task call() throws Exception {

                TaskRepository repository = new TaskRepository(datasource.getEntityManager());

                return repository.findByTitle(taskTitle).orElseThrow(IllegalAccessError::new);
            }

        };

        retrieveTaskTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "handleAssignTaskToInmate", event.getSource().getException());
            AlertHelper.showErrorAlert("Assigning Task", "Error assigning task, check the logs for more intel");
        });

        retrieveTaskTask.setOnSucceeded(event -> {

            Task task = (Task) event.getSource().getValue();

            task.addInmateEntity(inmateEntity);

            saveTask(task);

        });

        executor.execute(retrieveTaskTask);
    }

    private void saveTask(Task task) {
        javafx.concurrent.Task<Task> saveTaskTask = new javafx.concurrent.Task<Task>() {
            @Override
            protected Task call() throws Exception {
                TaskRepository repository = new TaskRepository(datasource.getEntityManager());

                return repository.save(task).orElseThrow(IllegalArgumentException::new);
            }
        };

        saveTaskTask.setOnFailed(workerStateEvent -> {
            Log.error(getClass().getSimpleName(), "saveTask", workerStateEvent.getSource().getException());
            AlertHelper.showErrorAlert("Assigning Task Filed", "Could not updated inmate tasks, check log for more intel");
        });

        saveTaskTask.setOnSucceeded(workerStateEvent -> closeStage());

        executor.execute(saveTaskTask);
    }

    public void setInmateEntity(Integer id) {

        javafx.concurrent.Task<InmateEntity> retrieveInmateEntityTask = new javafx.concurrent.Task<>() {

            @Override
            protected InmateEntity call() throws Exception {

                InmateRepository inmateRepository = new InmateRepository(datasource.getEntityManager());

                return inmateRepository.findById(id).orElseThrow(IllegalAccessError::new);
            }

        };

        retrieveInmateEntityTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "setInmateEntity", event.getSource().getException());
            AlertHelper.showErrorAlert("Retrieving Inmate", event.getSource().getException().getLocalizedMessage());
        });

        retrieveInmateEntityTask.setOnSucceeded(event -> {
            inmateEntity = (InmateEntity) event.getSource().getValue();

            submitButton.setDisable(false);

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle(inmateEntity.getName() + " - Assign Task | Prison Management Software");

        });

        executor.execute(retrieveInmateEntityTask);
    }

    public void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
