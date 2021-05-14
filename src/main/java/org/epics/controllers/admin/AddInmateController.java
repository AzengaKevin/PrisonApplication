package org.epics.controllers.admin;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.epics.data.Datasource;
import org.epics.data.entities.InmateEntity;
import org.epics.data.enums.Gender;
import org.epics.data.repositories.InmateRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddInmateController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> genderField;
    @FXML
    private TextField caseNumberField;
    @FXML
    private DatePicker convictionDateField;
    @FXML
    private DatePicker releaseDateField;
    @FXML
    private DatePicker dobField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;

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

        setupGenderField();

        cancelButton.setOnAction(event -> closeStage());

        submitButton.setOnAction(event -> handleAddInmateAction());

    }

    private void handleAddInmateAction() {

        String name = nameField.getText();
        String gender = genderField.getSelectionModel().getSelectedItem();
        String caseNumber = caseNumberField.getText();

        if (name.isEmpty()) {
            AlertHelper.showErrorAlert("Adding Inmate", "Inmate Name is required");
            nameField.requestFocus();
            return;
        }

        if (gender.isEmpty()) {
            AlertHelper.showErrorAlert("Adding Inmate", "Inmate Gender is required");
            genderField.requestFocus();
            return;
        }

        if (caseNumber.isEmpty()) {
            AlertHelper.showErrorAlert("Adding Inmate", "Inmate Case Number is required");
            caseNumberField.requestFocus();
            return;
        }

        if (dobField.getEditor().getText().isEmpty()) {
            AlertHelper.showErrorAlert("Adding Inmate", "Inmate Date Of Birth is required");
            dobField.requestFocus();
            return;
        }

        if (convictionDateField.getEditor().getText().isEmpty()) {
            AlertHelper.showErrorAlert("Adding Inmate", "Inmate Conviction Date is required");
            convictionDateField.requestFocus();
            return;
        }

        if (releaseDateField.getEditor().getText().isEmpty()) {
            AlertHelper.showErrorAlert("Adding Inmate", "Inmate Release Date is required");
            releaseDateField.requestFocus();
            return;
        }

        LocalDate dobLocalDate = dobField.getValue();
        Instant dobInstant = Instant.from(dobLocalDate.atStartOfDay(ZoneId.systemDefault()));
        Date dob = Date.from(dobInstant);

        LocalDate convictionLocalDate = convictionDateField.getValue();
        Instant convictionInstant = Instant.from(convictionLocalDate.atStartOfDay(ZoneId.systemDefault()));
        Date convictionDate = Date.from(convictionInstant);

        LocalDate releaseLocalDate = releaseDateField.getValue();
        Instant releaseInstant = Instant.from(releaseLocalDate.atStartOfDay(ZoneId.systemDefault()));
        Date releaseDate = Date.from(releaseInstant);

        InmateEntity inmateEntity = new InmateEntity(name, caseNumber, Gender.fromString(gender), null, dob, convictionDate, releaseDate);

        Task<InmateEntity> addInmateEntityTask = new Task<>() {
            @Override
            protected InmateEntity call() throws Exception {

                InmateRepository inmateRepository = new InmateRepository(datasource.getEntityManager());

                return inmateRepository.save(inmateEntity).orElseThrow(Exception::new);
            }
        };

        addInmateEntityTask.setOnFailed(event -> {
            AlertHelper.showErrorAlert("Adding Inmate", event.getSource().getException().getLocalizedMessage());
            Log.error(getClass().getSimpleName(), "Adding Inmate Failed", event.getSource().getException());
        });

        addInmateEntityTask.setOnSucceeded(event -> {
            AlertHelper.showInformationAlert("Adding Inmate", "Inmate Added Successfully");
            closeStage();
        });

        executor.execute(addInmateEntityTask);

    }

    private void setupGenderField() {
        genderField.setItems(
                FXCollections.observableArrayList(
                        Arrays.stream(
                                Gender.values())
                                .map(Gender::toString)
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
