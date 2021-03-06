package org.epics.controllers.doctor;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.epics.data.Datasource;
import org.epics.data.entities.HealthRecordEntity;
import org.epics.data.entities.UserEntity;
import org.epics.data.repositories.UserRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddHealthRecordController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField diseaseField;
    @FXML
    private DatePicker diagnosisDateField;
    @FXML
    private TextArea prescriptionField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;

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

        submitButton.setOnAction(event -> handAddUserHealthRecord());

        cancelButton.setOnAction(event -> closeStage());

    }

    private void handAddUserHealthRecord() {

        if (userEntity == null) {
            AlertHelper.showErrorAlert("Adding Health Record", "User Is Empty");
            return;
        }

        String disease = diseaseField.getText();
        String prescription = prescriptionField.getText();
        String diagnosisDateStr = diagnosisDateField.getEditor().getText();

        if (disease.isEmpty() || prescription.isEmpty() || diagnosisDateStr.isEmpty()) {
            AlertHelper.showErrorAlert("Adding Health Record", "All fields are required");
            return;
        }

        Date diagnosisDate = null;

        if (!diagnosisDateField.getEditor().getText().isEmpty()) {

            LocalDate dobLocalDate = diagnosisDateField.getValue();
            Instant dobInstant = Instant.from(dobLocalDate.atStartOfDay(ZoneId.systemDefault()));
            diagnosisDate = Date.from(dobInstant);
        }

        userEntity.addHealthRecord(new HealthRecordEntity(disease, prescription, diagnosisDate, null));

        Task<UserEntity> addHealthRecordTask = new Task<>() {
            @Override
            protected UserEntity call() throws Exception {

                UserRepository userRepository = new UserRepository(datasource.getEntityManager());

                return userRepository.save(userEntity).orElseThrow(IllegalAccessError::new);
            }
        };

        addHealthRecordTask.setOnFailed(event -> {
            Log.error(getClass().getSimpleName(), "handAddUserHealthRecord", event.getSource().getException());
            AlertHelper.showErrorAlert("Adding User Health Record", event.getSource().getException().getLocalizedMessage());
        });

        addHealthRecordTask.setOnSucceeded(event -> {
            AlertHelper.showInformationAlert("Saving User Health Record", "User Health Record Saved");
            closeStage();
        });

        executor.execute(addHealthRecordTask);

    }

    public void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
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
