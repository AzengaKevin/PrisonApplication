package org.epics.controllers.admin;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.epics.data.Datasource;
import org.epics.data.entities.StaffEntity;
import org.epics.data.enums.Role;
import org.epics.data.repositories.StaffRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddStaffController implements Initializable {


    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;
    @FXML
    private TextField nameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleField;

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

        setUpRolesField();

        cancelButton.setOnAction(event -> closeStage());

        submitButton.setOnAction(event -> handleAddStaffEvent());
    }

    private void handleAddStaffEvent() {

        String name = nameField.getText();
        String username = usernameField.getText();
        String role = roleField.getSelectionModel().getSelectedItem();
        String password = passwordField.getText();

        if (name.isEmpty()) {
            AlertHelper.showErrorAlert("Add Staff", "Name is required");
            nameField.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            AlertHelper.showErrorAlert("Add Staff", "Username is required");
            usernameField.requestFocus();
            return;
        }

        if (role.isEmpty()) {
            AlertHelper.showErrorAlert("Add Staff", "Role is required");
            roleField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            AlertHelper.showErrorAlert("Add Staff", "Password is required");
            passwordField.requestFocus();
            return;
        }

        StaffEntity staffEntity = new StaffEntity(name, username, password, Role.fromFromSlug(role));

        Task<StaffEntity> addUserTask = new Task<>() {
            @Override
            protected StaffEntity call() throws Exception {
                StaffRepository userRepository = new StaffRepository(datasource.getEntityManager());

                return userRepository.save(staffEntity).orElseThrow(Exception::new);
            }
        };

        addUserTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "handleAddStaffEvent", event.getSource().getException());
            AlertHelper.showErrorAlert("Adding Staff", event.getSource().getException().getLocalizedMessage());
        });

        addUserTask.setOnSucceeded(event -> {
            AlertHelper.showInformationAlert("Adding Staff", "Staff Successfully added");
            closeStage();
        });

        executor.execute(addUserTask);
    }

    private void setUpRolesField() {
        roleField.setItems(
                FXCollections.observableArrayList(
                        Arrays.stream(Role.values())
                                .map(Role::getSlug)
                                .toList()
                )
        );

    }

    public void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        //stage.getOnCloseRequest().notify();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

}
