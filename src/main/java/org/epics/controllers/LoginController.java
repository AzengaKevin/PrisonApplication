package org.epics.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {

                Optional<User> maybeUser = userRepository.findByUsername(username);

                if (maybeUser.isPresent()) {
                    User user = maybeUser.get();

                    return user.getPassword().equals(password);
                }

                return false;
            }
        };

        loginTask.setOnFailed(event -> System.err.println("Login Failed"));

        loginTask.setOnSucceeded(event -> {
            //@TODO Send to Relevant Dashboard
            System.out.println("User Logged In successfully");

            Boolean loginWasSuccessful = (Boolean) event.getSource().getValue();

            if (loginWasSuccessful) {

                AlertHelper.showInformationAlert("Login Attempt", "Login was successful");
            } else {
                AlertHelper.showErrorAlert("Login Attempt", "Invalid login credentials, try again");
            }
        });

        executor.execute(loginTask);

    }
}
