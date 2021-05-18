package org.epics.controllers.doctor;

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
import org.epics.data.entities.HealthRecordEntity;
import org.epics.data.entities.UserEntity;
import org.epics.data.repositories.UserRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HealthRecordsController implements Initializable {

    @FXML
    private TextField searchNameField;
    @FXML
    private Button searchUserButton;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> nameCol;

    @FXML
    private Button addHealthRecordButton;

    @FXML
    private TableView<HealthRecord> healthRecordsTable;
    @FXML
    private TableColumn<HealthRecord, String> diseaseCol;
    @FXML
    private TableColumn<HealthRecord, String> diagnosisDateCol;

    @FXML
    private MenuItem addRecordMenuItem;
    @FXML
    private MenuItem showRecordMenuItem;
    @FXML
    private AnchorPane rootPane;

    @FXML
    private MenuItem checkPrescriptionMenuItem;

    private Datasource datasource;
    private Executor executor;

    private User user;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        datasource = Datasource.getInstance();

        executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        addHealthRecordButton.setDisable(true);

        associateColumnsToProperties();

        retrieveAndShowUsers();

        addRecordMenuItem.setOnAction(event -> {

            User user = usersTable.getSelectionModel().getSelectedItem();

            if (user != null) {

                this.user = user;

                addHealthRecordButton.setDisable(false);

                handleAddHealthRecord();
            }
        });

        showRecordMenuItem.setOnAction(event -> {

            User user = usersTable.getSelectionModel().getSelectedItem();

            if (user != null) {

                this.user = user;

                addHealthRecordButton.setDisable(false);

                showCurrentUserHealthRecords();
            }

        });

        addHealthRecordButton.setOnAction(event -> handleAddHealthRecord());

        checkPrescriptionMenuItem.setOnAction(event -> {
            HealthRecord healthRecord = healthRecordsTable.getSelectionModel().getSelectedItem();

            AlertHelper.showInformationAlert("Prescription", healthRecord.getPrescription());
        });

        searchUserButton.setOnAction(event -> {

            String query = searchNameField.getText();

            if (query.isEmpty()) {
                AlertHelper.showErrorAlert("Search User", "No search query");
                return;
            }

            searchUser(query);

        });
    }

    private void searchUser(String query) {
        Task<List<UserEntity>> retrieveUsersTask = new Task<>() {
            @Override
            protected List<UserEntity> call() throws Exception {

                UserRepository userRepository = new UserRepository(datasource.getEntityManager());

                return userRepository.searchByName(query);
            }
        };

        retrieveUsersTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "retrieveAndShowUsers:failed", event.getSource().getException());
            AlertHelper.showErrorAlert("Retrieving and Showing User", event.getSource().getException().getLocalizedMessage());
        });

        retrieveUsersTask.setOnSucceeded(event -> {
            List<UserEntity> userEntityList = (List<UserEntity>) event.getSource().getValue();

            List<User> userList = userEntityList.stream().map(userEntity -> new User(
                    userEntity.getId(),
                    userEntity.getName(),
                    userEntity.getGroup()
            )).toList();

            usersTable.setItems(FXCollections.observableArrayList(userList));
        });

        executor.execute(retrieveUsersTask);
    }

    private void showCurrentUserHealthRecords() {

        if (user != null) {

            Task<UserEntity> getUserEntityTask = new Task<>() {
                @Override
                protected UserEntity call() throws Exception {

                    UserRepository userRepository = new UserRepository(datasource.getEntityManager());

                    return userRepository.findById(user.getId()).orElseThrow(IllegalAccessError::new);
                }
            };

            getUserEntityTask.setOnFailed(event -> {
                Log.error(getClass().getSimpleName(), "setUserEntity", event.getSource().getException());
                AlertHelper.showErrorAlert("Setting User", event.getSource().getException().getLocalizedMessage());
            });

            getUserEntityTask.setOnSucceeded(event -> {
                UserEntity userEntity = (UserEntity) event.getSource().getValue();

                List<HealthRecordEntity> healthRecordEntityList = userEntity.getHealthRecordEntityList();

                List<HealthRecord> healthRecordList = healthRecordEntityList.stream()
                        .map(healthRecordEntity -> new HealthRecord(
                                healthRecordEntity.getDisease(),
                                healthRecordEntity.getPrescription(),
                                dateFormat.format(healthRecordEntity.getDiagnosisDate())
                        )).toList();

                healthRecordsTable.setItems(FXCollections.observableArrayList(healthRecordList));
            });

            executor.execute(getUserEntityTask);
        }
    }

    private void associateColumnsToProperties() {
        //Users Table
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        //Health Records Table
        diseaseCol.setCellValueFactory(new PropertyValueFactory<>("disease"));
        diagnosisDateCol.setCellValueFactory(new PropertyValueFactory<>("diagnosisDate"));
    }

    private void handleAddHealthRecord() {

        if (user != null) {

            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/doctor/AddHealthRecord.fxml"));

                Parent root = loader.load();

                AddHealthRecordController controller = loader.getController();

                controller.setUserEntity(user.getId());

                Stage stage = new Stage();
                stage.setTitle("Add Health Record | Prison Management Software");

                Scene scene = new Scene(root);

                scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());

                stage.setScene(scene);

                stage.setOnCloseRequest(event -> showCurrentUserHealthRecords());

                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(rootPane.getScene().getWindow());

                stage.show();


            } catch (Exception exception) {

                Log.error(getClass().getSimpleName(), "handleAddHealthRecord", exception);

            }

        } else {
            AlertHelper.showErrorAlert("Add Health Record", "You must select a user");
        }
    }

    private void retrieveAndShowUsers() {
        Task<List<UserEntity>> retrieveUsersTask = new Task<>() {
            @Override
            protected List<UserEntity> call() throws Exception {

                UserRepository userRepository = new UserRepository(datasource.getEntityManager());

                return userRepository.findAll();
            }
        };

        retrieveUsersTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "retrieveAndShowUsers:failed", event.getSource().getException());
            AlertHelper.showErrorAlert("Retrieving and Showing User", event.getSource().getException().getLocalizedMessage());
        });

        retrieveUsersTask.setOnSucceeded(event -> {
            List<UserEntity> userEntityList = (List<UserEntity>) event.getSource().getValue();

            List<User> userList = userEntityList.stream().map(userEntity -> new User(
                    userEntity.getId(),
                    userEntity.getName(),
                    userEntity.getGroup()
            )).toList();

            usersTable.setItems(FXCollections.observableArrayList(userList));
        });

        executor.execute(retrieveUsersTask);
    }


    public static class User {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty group;

        public User(int id, String name, String group) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.group = new SimpleStringProperty(group);
        }

        public int getId() {
            return id.get();
        }

        public String getName() {
            return name.get();
        }


        public String getGroup() {
            return group.get();
        }
    }

    public static class HealthRecord {

        private final SimpleStringProperty disease;
        private final SimpleStringProperty prescription;
        private final SimpleStringProperty diagnosisDate;

        public HealthRecord(String disease, String prescription, String diagnosisDate) {

            this.disease = new SimpleStringProperty(disease);
            this.prescription = new SimpleStringProperty(prescription);
            this.diagnosisDate = new SimpleStringProperty(diagnosisDate);

        }

        public String getDisease() {
            return disease.get();
        }

        public String getPrescription() {
            return prescription.get();
        }

        public String getDiagnosisDate() {
            return diagnosisDate.get();
        }

    }
}
