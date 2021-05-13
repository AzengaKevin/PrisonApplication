package org.epics.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.epics.data.entities.User;
import org.epics.data.repositories.UserRepository;
import org.epics.helpers.AlertHelper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginController implements Initializable {

    final private EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("PrisonMainUnit");
    final private EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
    final private UserRepository userRepository = new UserRepository(entityManager);

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button loginButton;

    private Executor executor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        cancelButton.setOnAction(actionEvent -> System.exit(0));

        loginButton.setOnAction(actionEvent -> processLogin());

    }

    private void processLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            usernameField.requestFocus();
            AlertHelper.showErrorAlert("Login Attempt", "Username field is required");
            return;
        }

        if (password.isEmpty()) {
            usernameField.requestFocus();
            AlertHelper.showErrorAlert("Login Attempt", "Password field is required");
            return;
        }

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {

                Optional<User> maybeUser = userRepository.findByUsername(username);

                return maybeUser.orElse(null);

            }
        };

        loginTask.setOnFailed(event -> System.err.println("Login Failed"));

        loginTask.setOnSucceeded(event -> {

            if (event.getSource().getValue() instanceof User) {
                User user = (User) event.getSource().getValue();

                if (user.getPassword().equals(password)) {

                    switch (user.getRole()) {
                        case Admin -> {
                            try {

                                changeStage("/layouts/admin/Dashboard.fxml", "Admin Dashboard | Prison Management System");

                            } catch (Exception exception) {

                                System.err.println(exception.getLocalizedMessage());
                            }
                        }

                        case Doctor -> {
                            System.out.println("Doctor");
                        }

                        case TaskManager -> {
                            System.out.println("Task Manager");

                        }

                        case Warden -> {

                            System.out.println("Warden");
                        }

                        default -> {

                            System.out.println("No Idea");

                        }
                    }

                } else {

                    AlertHelper.showErrorAlert("Login Attempt", "Invalid login credentials, try again");

                }

            } else {

                AlertHelper.showErrorAlert("Login Attempt", "Invalid login credentials, try again");

            }
        });

        executor.execute(loginTask);

    }

    public void changeStage(String location, String title) throws Exception {

        Parent parent = FXMLLoader.load(getClass().getResource(location));

        Stage stage = new Stage();
        stage.setTitle(title);
        Scene scene = new Scene(parent);

        scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());

        stage.setScene(scene);

        ((Stage) rootPane.getScene().getWindow()).close();

        stage.show();
    }
}
