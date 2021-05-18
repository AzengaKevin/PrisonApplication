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
import org.epics.data.Datasource;
import org.epics.data.entities.StaffEntity;
import org.epics.data.repositories.StaffRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.AuthHelper;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginController implements Initializable {

    private Datasource datasource;

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

        datasource = Datasource.getInstance();

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

        Task<StaffEntity> loginTask = new Task<>() {
            @Override
            protected StaffEntity call() throws Exception {

                StaffRepository staffRepository = new StaffRepository(datasource.getEntityManager());

                Optional<StaffEntity> maybeStaff = staffRepository.findByUsername(username);

                return maybeStaff.orElse(null);

            }
        };

        loginTask.setOnFailed(event -> {
            System.err.println("Login Failed");
            AlertHelper.showErrorAlert("Login Attempt", event.getSource().getException().getLocalizedMessage());
        });

        loginTask.setOnSucceeded(event -> {

            if (event.getSource().getValue() instanceof StaffEntity) {
                StaffEntity staffEntity = (StaffEntity) event.getSource().getValue();

                if (staffEntity.getPassword().equals(password)) {


                    try {

                        AuthHelper.loginStaff(staffEntity);

                        switch (staffEntity.getRole()) {
                            case Admin -> changeStage("/layouts/admin/Dashboard.fxml", "Admin Dashboard | Prison Management System");

                            case Doctor -> changeStage("/layouts/doctor/Dashboard.fxml", "Doctor Dashboard | Prison Management System");

                            case Warden -> changeStage("/layouts/warden/Dashboard.fxml", "Warden Dashboard | Prison Management System");

                            default -> System.out.println("No Idea Which Stuff You're");
                        }

                    } catch (Exception exception) {

                        System.err.println(exception.getLocalizedMessage());
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
