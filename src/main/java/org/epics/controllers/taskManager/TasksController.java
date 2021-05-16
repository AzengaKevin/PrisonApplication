package org.epics.controllers.taskManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.epics.data.Datasource;
import org.epics.data.entities.Task;
import org.epics.data.repositories.TaskRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TasksController implements Initializable {

    public AnchorPane rootPane;
    public Button addTaskButton;
    public TableView<TaskItem> tasksTable;
    public TableColumn<TaskItem, Integer> idCol;
    public TableColumn<TaskItem, String> titleCol;
    public TableColumn<TaskItem, String> descriptionCol;
    public TableColumn<TaskItem, String> startDateCol;
    public TableColumn<TaskItem, String> endDateCol;
    public TableColumn<TaskItem, Integer> inmatesCountCol;

    private Datasource datasource;
    private Executor executor;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyy", Locale.ENGLISH);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        datasource = Datasource.getInstance();

        executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        addTaskButton.setOnAction(event -> launchAddTaskWindow());

        associateTableColumns();

        retrieveAndShowAllTasks();
    }

    private void launchAddTaskWindow() {
        try {

            Parent parent = FXMLLoader.load(getClass().getResource("/layouts/taskManager/AddTask.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Add Inmate | Prison Management Software");
            Scene scene = new Scene(parent);

            scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());

            stage.setScene(scene);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(rootPane.getScene().getWindow());

            stage.setOnCloseRequest(event -> retrieveAndShowAllTasks());

            stage.show();

        } catch (Exception exception) {
            Log.error(getClass().getSimpleName(), "launchAddStaffStage", exception);
        }
    }

    private void retrieveAndShowAllTasks() {

        javafx.concurrent.Task<List<Task>> retrieveTasksTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Task> call() throws Exception {
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

            List<TaskItem> taskItemList = taskList.stream().map(task -> new TaskItem(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    dateFormatter.format(task.getStartDate()),
                    dateFormatter.format(task.getEndDate()),
                    task.getInmateEntityList().size()
            )).toList();

            tasksTable.setItems(FXCollections.observableArrayList(taskItemList));

        });

        executor.execute(retrieveTasksTask);
    }

    private void associateTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        inmatesCountCol.setCellValueFactory(new PropertyValueFactory<>("inmateCount"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

    }

    public static class TaskItem {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty title;
        private final SimpleStringProperty description;
        private final SimpleStringProperty startDate;
        private final SimpleStringProperty endDate;
        private final SimpleIntegerProperty inmateCount;

        public TaskItem(int id, String title, String description, String startDate, String endDate, int inmateCount) {

            this.id = new SimpleIntegerProperty(id);
            this.title = new SimpleStringProperty(title);
            this.description = new SimpleStringProperty(description);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
            this.inmateCount = new SimpleIntegerProperty(inmateCount);
        }

        public int getId() {
            return id.get();
        }

        public String getTitle() {
            return title.get();
        }

        public String getDescription() {
            return description.get();
        }

        public String getStartDate() {
            return startDate.get();
        }

        public String getEndDate() {
            return endDate.get();
        }

        public int getInmateCount() {
            return inmateCount.get();
        }
    }

}
