package org.epics.controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.epics.data.Datasource;
import org.epics.data.entities.StaffEntity;
import org.epics.data.enums.Role;
import org.epics.data.repositories.StaffRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StaffController implements Initializable {

    private Datasource datasource;

    @FXML
    private ProgressIndicator staffProgressIndicator;
    @FXML
    private Button addStaffMember;
    @FXML
    private TableView<User> staffMembersTable;
    @FXML
    private TableColumn<User, Integer> idCol;
    @FXML
    private TableColumn<User, String> nameCol;
    @FXML
    private TableColumn<User, String> usernameCol;
    @FXML
    private TableColumn<User, String> roleCol;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private MenuItem deleteMemberMenuItem;

    private Executor executor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        datasource = Datasource.getInstance();

        executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        staffProgressIndicator.setVisible(false);

        associateCols();

        retrieveAndShowStaff();

        addStaffMember.setOnAction(event -> launchAddStaffStage());

        deleteMemberMenuItem.setOnAction(event -> {
            User user = staffMembersTable.getSelectionModel().getSelectedItem();

            if (user == null) return;

            Task<StaffEntity> retrieveStaff = new Task<>() {
                @Override
                protected StaffEntity call() throws Exception {
                    StaffRepository repository = new StaffRepository(datasource.getEntityManager());
                    return repository.findById(user.getId()).orElseThrow(IllegalAccessError::new);
                }
            };

            retrieveStaff.setOnFailed(workerStateEvent -> {
                Log.error(getClass().getSimpleName(), "initialize", workerStateEvent.getSource().getException());
                AlertHelper.showErrorAlert("Deleting Staff", "Error deleting Staff Member, check logs");
            });

            retrieveStaff.setOnSucceeded(workerStateEvent -> {

                StaffRepository repository = new StaffRepository(datasource.getEntityManager());

                StaffEntity staffEntity = (StaffEntity) workerStateEvent.getSource().getValue();

                repository.delete(staffEntity);

                retrieveAndShowStaff();

            });

            executor.execute(retrieveStaff);
        });
    }

    private void launchAddStaffStage() {
        try {

            Parent parent = FXMLLoader.load(getClass().getResource("/layouts/admin/AddStaff.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Add Staff | Prison Management Software");
            Scene scene = new Scene(parent);

            scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());

            stage.setScene(scene);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(rootPane.getScene().getWindow());

            stage.setOnCloseRequest(event -> retrieveAndShowStaff());

            stage.show();

        } catch (Exception exception) {
            Log.error(getClass().getSimpleName(), "launchAddStaffStage", exception);
        }
    }

    private void retrieveAndShowStaff() {
        Task<List<StaffEntity>> staffTask = new Task<>() {
            @Override
            protected List<StaffEntity> call() throws Exception {

                StaffRepository staffRepository = new StaffRepository(datasource.getEntityManager());
                return staffRepository.findByRoles(List.of(Role.Admin, Role.Doctor, Role.Guard));
            }
        };

        staffTask.setOnFailed(event -> {

            staffProgressIndicator.setVisible(false);

            Log.error(getClass().getSimpleName(), "retrieveAndShowStaff", event.getSource().getException());
        });

        staffTask.setOnSucceeded(event -> {

            staffProgressIndicator.setVisible(false);

            List<StaffEntity> staffEntityList = (List<StaffEntity>) event.getSource().getValue();

            List<User> tableUsers = staffEntityList.stream().map(
                    staffEntity -> new User(
                            staffEntity.getId(),
                            staffEntity.getName(),
                            staffEntity.getUsername(),
                            staffEntity.getRole()
                    )
            ).toList();

            staffMembersTable.setItems(FXCollections.observableList(tableUsers));
        });

        staffProgressIndicator.setVisible(true);
        executor.execute(staffTask);
    }

    private void associateCols() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    public static class User {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty username;
        private final SimpleStringProperty role;

        public User(int id, String name, String username, Role role) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.username = new SimpleStringProperty(username);
            this.role = new SimpleStringProperty(role.getSlug());
        }

        public int getId() {
            return id.get();
        }


        public String getName() {
            return name.get();
        }

        public String getUsername() {
            return username.get();
        }

        public String getRole() {
            return role.get();
        }
    }
}
