package org.epics.controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.epics.data.enums.Role;
import org.epics.data.repositories.UserRepository;
import org.epics.helpers.Log;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StaffController implements Initializable {

    final private EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("PrisonMainUnit");
    final private EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
    final private UserRepository userRepository = new UserRepository(entityManager);

    public ProgressIndicator staffProgressIndicator;
    public Button addStaffMember;
    public TableView<User> staffMembersTable;
    public TableColumn<User, Integer> idCol;
    public TableColumn<User, String> nameCol;
    public TableColumn<User, String> usernameCol;
    public TableColumn<User, String> roleCol;
    public TableColumn<User, String> passwordCol;

    private Executor executor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        staffProgressIndicator.setVisible(false);

        associateCols();

        retrieveAndShowStaff();
    }

    private void retrieveAndShowStaff() {
        Task<List<org.epics.data.entities.User>> staffTask = new Task<>() {
            @Override
            protected List<org.epics.data.entities.User> call() throws Exception {
                return userRepository.findByRoles(List.of(Role.Admin, Role.Doctor, Role.TaskManager, Role.Warden));
            }
        };

        staffTask.setOnFailed(event -> {

            staffProgressIndicator.setVisible(false);

            Log.error(getClass().getSimpleName(), "retrieveAndShowStaff", event.getSource().getException());
        });

        staffTask.setOnSucceeded(event -> {

            staffProgressIndicator.setVisible(false);

            List<org.epics.data.entities.User> userList = (List<org.epics.data.entities.User>) event.getSource().getValue();

            List<User> tableUsers = userList.stream().map(
                    user -> new User(
                            user.getId(),
                            user.getName(),
                            user.getUsername(),
                            user.getRole(),
                            user.getPassword()
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
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
    }

    public static class User {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty username;
        private final SimpleStringProperty role;
        private final SimpleStringProperty password;

        public User(int id, String name, String username, Role role, String password) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.username = new SimpleStringProperty(username);
            this.password = new SimpleStringProperty(password);
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

        public String getPassword() {
            return password.get();
        }

        public String getRole() {
            return role.get();
        }
    }
}
