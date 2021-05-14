package org.epics.controllers.doctor;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.epics.data.Datasource;
import org.epics.data.entities.UserEntity;
import org.epics.data.repositories.UserRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UsersController implements Initializable {

    public TableView<User> usersTable;
    public TableColumn<User, Integer> idCol;
    public TableColumn<User, String> nameCol;
    public TableColumn<User, String> groupCol;

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

        associateColumnsToProperties();

        retrieveAndShowUsers();

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

    private void associateColumnsToProperties() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        groupCol.setCellValueFactory(new PropertyValueFactory<>("group"));
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
}
