package org.epics.controllers;

import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.epics.data.Datasource;
import org.epics.data.entities.StaffEntity;
import org.epics.data.repositories.StaffRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.AuthHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsController implements Initializable {

    public TextField nameField;
    public TextField usernameField;
    public PasswordField passwordField;
    public Button cancelButton;
    public Button submitButton;
    public AnchorPane rootPane;

    private Datasource datasource;
    private Executor executor;

    private StaffEntity staffEntity;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        datasource = Datasource.getInstance();

        executor = Executors.newCachedThreadPool(r -> {

            Thread thread = new Thread(r);
            thread.setDaemon(true);

            return thread;
        });

        try {

            staffEntity = AuthHelper.getLoggedInStaff().orElseThrow(IllegalAccessError::new);

            nameField.setText(staffEntity.getName());
            usernameField.setText(staffEntity.getUsername());
            passwordField.setText(staffEntity.getPassword());

        } catch (Exception exception) {
            Log.error(getClass().getSimpleName(), "initialize", exception);
            AlertHelper.showErrorAlert("Get Current User", "Retrieving Currently authenticated user, failed check the logs for more intel");
        }

        cancelButton.setOnAction(event -> closeStage());
        submitButton.setOnAction(event -> updateSettings());

    }


    private void updateSettings() {

        String name = nameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (name.isEmpty()) {
            AlertHelper.showErrorAlert("Update Settings", "Name is required");
            nameField.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            AlertHelper.showErrorAlert("Update Settings", "Username is required");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            AlertHelper.showErrorAlert("Update Settings", "Password is required");
            passwordField.requestFocus();
            return;
        }

        staffEntity.setName(name);
        staffEntity.setUsername(username);
        staffEntity.setPassword(password);

        Task<StaffEntity> addUserTask = new Task<>() {
            @Override
            protected StaffEntity call() throws Exception {
                StaffRepository userRepository = new StaffRepository(datasource.getEntityManager());
                return userRepository.update(staffEntity).orElseThrow(Exception::new);
            }
        };

        addUserTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "updateSettings", event.getSource().getException());
            AlertHelper.showErrorAlert("Update Settings", event.getSource().getException().getLocalizedMessage());
        });

        addUserTask.setOnSucceeded(event -> {
            AlertHelper.showInformationAlert("Update Settings", "Settings Updated Successfully");
            closeStage();
        });

        executor.execute(addUserTask);
    }

    public void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

}
