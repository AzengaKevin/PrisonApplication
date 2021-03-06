package org.epics.controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
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

    public TableView<User> usersTable;
    public TableColumn<User, String> nameCol;

    public TableView<HealthRecord> healthRecordsTable;
    public TableColumn<HealthRecord, String> diseaseCol;
    public TableColumn<HealthRecord, String> diagnosisDateCol;

    public MenuItem showRecordMenuItem;
    public MenuItem checkDescriptionMenuItem;
    public AnchorPane rootPane;

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

        associateColumnsToProperties();

        retrieveAndShowUsers();

        showRecordMenuItem.setOnAction(event -> {

            User user = usersTable.getSelectionModel().getSelectedItem();

            if (user != null) {

                this.user = user;

                showCurrentUserHealthRecords();
            }

        });
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
        private final SimpleStringProperty diagnosisDate;

        public HealthRecord(String disease, String diagnosisDate) {

            this.disease = new SimpleStringProperty(disease);
            this.diagnosisDate = new SimpleStringProperty(diagnosisDate);

        }

        public String getDisease() {
            return disease.get();
        }

        public String getDiagnosisDate() {
            return diagnosisDate.get();
        }

    }

}
