package org.epics.controllers.admin;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.element.Table;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.epics.data.Datasource;
import org.epics.data.entities.Task;
import org.epics.data.repositories.TaskRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;
import org.epics.internals.printing.TablePrinter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TasksController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button addTaskButton;
    @FXML
    private TableView<TaskItem> tasksTable;
    @FXML
    private TableColumn<TaskItem, Integer> idCol;
    @FXML
    private TableColumn<TaskItem, String> titleCol;
    @FXML
    private TableColumn<TaskItem, String> descriptionCol;
    @FXML
    private TableColumn<TaskItem, String> startDateCol;
    @FXML
    private TableColumn<TaskItem, String> endDateCol;
    @FXML
    private TableColumn<TaskItem, Integer> inmatesCountCol;
    @FXML
    private Button printTasksButton;

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

        printTasksButton.setOnAction(event -> handlePrintingTasksAction());
    }

    private void handlePrintingTasksAction() {

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

            try {
                //Choose a directory
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select Location For the Report File");
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                final File selectedDir = directoryChooser.showDialog(rootPane.getScene().getWindow());

                //Dynamic filename
                String filename = selectedDir.getAbsolutePath() + "/" + String.valueOf(System.currentTimeMillis()).concat("-tasks-report.pdf");

                //Create a instance of Table printer passing the filename
                TablePrinter tablePrinter = new TablePrinter(filename);

                tablePrinter.initDocument(20, PageSize.A4.rotate());

                tablePrinter.addTitle("Tasks");

                Table table = new Table(4);

                //Add the headers
                table.addHeaderCell("Title");
                table.addHeaderCell("Description");
                table.addHeaderCell("Start Date");
                table.addHeaderCell("End Date");

                taskList.forEach(task -> {
                    table.addCell(task.getTitle());
                    table.addCell(task.getDescription());
                    table.addCell(dateFormatter.format(task.getStartDate()));
                    table.addCell(dateFormatter.format(task.getEndDate()));

                });

                tablePrinter.addBlockElement(table);

                tablePrinter.print();

                AlertHelper.showInformationAlert("Printing", "Report printed Successfully");

            } catch (IOException e) {
                Log.error(getClass().getSimpleName(), "Printing", e);
                AlertHelper.showErrorAlert("Printing Stuff", "Printing failed, try again");
            }


        });

        executor.execute(retrieveTasksTask);
    }

    private void launchAddTaskWindow() {
        try {

            Parent parent = FXMLLoader.load(getClass().getResource("/layouts/admin/AddTask.fxml"));

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
