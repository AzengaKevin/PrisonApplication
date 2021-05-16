package org.epics.controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.epics.data.Datasource;
import org.epics.data.entities.InmateEntity;
import org.epics.data.entities.Task;
import org.epics.data.repositories.InmateRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskManagementController implements Initializable {


    @FXML
    private AnchorPane rootPane;

    @FXML
    private TableView<InmateItem> inmatesTable;
    @FXML
    private TableColumn<InmateItem, String> inmateCol;
    @FXML
    private MenuItem viewTasksMenuItem;

    @FXML
    private TableView<TaskItem> inmateTaskTable;
    @FXML
    private TableColumn<TaskItem, String> titleCol;
    @FXML
    private TableColumn<TaskItem, String> descriptionCol;
    @FXML
    private TableColumn<TaskItem, String> statusCol;

    private Datasource datasource;
    private Executor executor;

    private InmateItem inmateItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        datasource = Datasource.getInstance();

        executor = Executors.newCachedThreadPool(r -> {

            Thread thread = new Thread(r);
            thread.setDaemon(true);

            return thread;
        });

        bindColumnsToProperty();

        retrieveInmates();

        viewTasksMenuItem.setOnAction(event -> {

            inmateItem = inmatesTable.getSelectionModel().getSelectedItem();

            handleShowInmateTasks();

        });
    }

    private void handleShowInmateTasks() {

        if (inmateItem != null) {

            javafx.concurrent.Task<InmateEntity> retrieveInmateEntityTask = new javafx.concurrent.Task<>() {

                @Override
                protected InmateEntity call() throws Exception {

                    InmateRepository inmateRepository = new InmateRepository(datasource.getEntityManager());

                    return inmateRepository.findById(inmateItem.getId()).orElseThrow(IllegalAccessError::new);
                }

            };

            retrieveInmateEntityTask.setOnFailed(event -> {
                Log.error(getClass().getSimpleName(), "setInmateEntity", event.getSource().getException());
                AlertHelper.showErrorAlert("Retrieving Inmate", event.getSource().getException().getLocalizedMessage());
            });

            retrieveInmateEntityTask.setOnSucceeded(event -> {
                InmateEntity inmateEntity = (InmateEntity) event.getSource().getValue();

                Set<Task> taskSet = inmateEntity.getTasks();

                List<TaskItem> taskItemList = taskSet.stream().map(TaskItem::new).toList();

                inmateTaskTable.setItems(FXCollections.observableArrayList(taskItemList));

            });

            executor.execute(retrieveInmateEntityTask);
        }
    }

    private void bindColumnsToProperty() {

        inmateCol.setCellValueFactory(new PropertyValueFactory<>("inmate"));

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void retrieveInmates() {
        javafx.concurrent.Task<List<InmateEntity>> retrieveInmatesTask = new javafx.concurrent.Task<List<InmateEntity>>() {
            @Override
            protected List<InmateEntity> call() throws Exception {
                InmateRepository repository = new InmateRepository(datasource.getEntityManager());
                return repository.findAll();
            }
        };

        retrieveInmatesTask.setOnFailed(workerStateEvent -> {
            Log.error(getClass().getSimpleName(), "retrieveInmates", workerStateEvent.getSource().getException());
            AlertHelper.showErrorAlert("Retrieving Inmates", "An error occurred during inmate retrieval, check the log for more intel");
        });

        retrieveInmatesTask.setOnSucceeded(workerStateEvent -> {
            List<InmateEntity> inmateEntityList = (List<InmateEntity>) workerStateEvent.getSource().getValue();

            List<InmateItem> inmateItemList = inmateEntityList.stream().map(InmateItem::new).toList();

            inmatesTable.setItems(FXCollections.observableArrayList(inmateItemList));
        });

        executor.execute(retrieveInmatesTask);
    }

    public static class InmateItem {

        private final SimpleStringProperty inmate;
        private final SimpleIntegerProperty id;

        public InmateItem(InmateEntity inmateEntity) {
            this.id = new SimpleIntegerProperty(inmateEntity.getId());
            this.inmate = new SimpleStringProperty(inmateEntity.getName());
        }

        public String getInmate() {
            return inmate.get();
        }

        public int getId() {
            return id.get();
        }
    }

    public static class TaskItem {

        private final SimpleStringProperty title;
        private final SimpleStringProperty description;
        private final SimpleStringProperty status;

        public TaskItem(Task task) {
            this.title = new SimpleStringProperty(task.getTitle());
            this.description = new SimpleStringProperty(task.getDescription());
            this.status = new SimpleStringProperty("Not Completed");
        }

        public String getTitle() {
            return title.get();
        }

        public String getDescription() {
            return description.get();
        }

        public String getStatus() {
            return status.get();
        }
    }
}
