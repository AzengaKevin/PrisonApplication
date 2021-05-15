package org.epics.controllers.doctor;

import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.epics.data.Datasource;
import org.epics.data.entities.UserEntity;
import org.epics.data.repositories.UserRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddHealthRecordController implements Initializable {

    public AnchorPane rootPane;
    public TextField diseaseField;
    public DatePicker diagnosisDateField;
    public DatePicker endDateField;
    public TextArea prescriptionField;
    public Button cancelButton;
    public Button submitButton;

    private UserEntity userEntity;
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

    }

    public void setUserEntity(int userEntityId) {

        Task<UserEntity> getUserEntityTask = new Task<>() {
            @Override
            protected UserEntity call() throws Exception {

                UserRepository userRepository = new UserRepository(datasource.getEntityManager());

                return userRepository.findById(userEntityId).orElseThrow(IllegalAccessError::new);
            }
        };

        getUserEntityTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "setUserEntity", event.getSource().getException());
            AlertHelper.showErrorAlert("Setting User", event.getSource().getException().getLocalizedMessage());
        });

        getUserEntityTask.setOnSucceeded(event -> {
            userEntity = (UserEntity) event.getSource().getValue();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle(userEntity.getName() + " - Add Health Record | Prison Management Software");

        });

        executor.execute(getUserEntityTask);
    }
}
