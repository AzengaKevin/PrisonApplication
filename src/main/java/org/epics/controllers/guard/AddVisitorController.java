package org.epics.controllers.guard;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.epics.data.Datasource;
import org.epics.data.entities.InmateEntity;
import org.epics.data.entities.Visitor;
import org.epics.data.enums.Gender;
import org.epics.data.repositories.InmateRepository;
import org.epics.data.repositories.VisitorRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddVisitorController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField nameField;
    @FXML
    private TextField hudumaNumberField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> genderField;
    @FXML
    public ComboBox<String> inmateField;
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
        setupInmatesField();

        cancelButton.setOnAction(event -> closeStage());
        submitButton.setOnAction(event -> handleAddVisitorOperation());

    }

    private void setupInmatesField() {

        Task<List<InmateEntity>> retrieveAllInmatesTask = new Task<>() {
            @Override
            protected List<InmateEntity> call() throws Exception {

                InmateRepository inmateRepository = new InmateRepository(datasource.getEntityManager());

                return inmateRepository.findAll();
            }
        };

        retrieveAllInmatesTask.setOnFailed(workerStateEvent -> {
            Log.error(getClass().getSimpleName(), "setupInmatesField", workerStateEvent.getSource().getException());
            AlertHelper.showErrorAlert("Getting Inmates", "Could not retrieve all inmates, check the logs for more intel");
        });

        retrieveAllInmatesTask.setOnSucceeded(workerStateEvent -> {

            List<InmateEntity> inmateEntityList = (List<InmateEntity>) workerStateEvent.getSource().getValue();
            List<String> inmateNamesList = inmateEntityList.stream().map(InmateEntity::getName).toList();

            inmateField.setItems(FXCollections.observableArrayList(inmateNamesList));
        });

        executor.execute(retrieveAllInmatesTask);

    }

    private void setupGenderField() {
        genderField.setItems(FXCollections.observableArrayList(Arrays.stream(Gender.values()).map(Gender::toString).toList()));
    }

    public void handleAddVisitorOperation() {

        String name = nameField.getText();
        String hudumaNumber = hudumaNumberField.getText();
        String phone = phoneField.getText();
        String dobText = dobField.getEditor().getText();
        String gender = genderField.getSelectionModel().getSelectedItem();
        String inmateName = inmateField.getSelectionModel().getSelectedItem();

        if (name.isEmpty() || hudumaNumber.isEmpty() || phone.isEmpty() || dobText.isEmpty() || gender.isEmpty() || inmateName.isEmpty()) {
            AlertHelper.showErrorAlert("Adding Visitor", "All fields are required");
            return;
        }

        LocalDate dobLocalDate = dobField.getValue();
        Instant dobInstant = Instant.from(dobLocalDate.atStartOfDay(ZoneId.systemDefault()));
        Date dob = Date.from(dobInstant);

        Task<InmateEntity> retrieveSingleInmate = new Task<>() {
            @Override
            protected InmateEntity call() throws Exception {

                InmateRepository inmateRepository = new InmateRepository(datasource.getEntityManager());
                return inmateRepository.findByName(inmateName).orElseThrow(IllegalAccessError::new);
            }
        };

        Visitor visitor = new Visitor(name, Gender.fromString(gender), dob, hudumaNumber, phone);

        retrieveSingleInmate.setOnFailed(workerStateEvent -> {
            Log.error(getClass().getSimpleName(), "handleAddVisitorOperation", workerStateEvent.getSource().getException());
            AlertHelper.showErrorAlert("Adding Visitor Operation", "Failed to identify selected inmate, check the logs for more information");
        });

        retrieveSingleInmate.setOnSucceeded(workerStateEvent -> {
            InmateEntity inmateEntity = (InmateEntity) workerStateEvent.getSource().getValue();
            visitor.setInmateEntity(inmateEntity);
            handleAddingVisitor(visitor);
        });

        executor.execute(retrieveSingleInmate);

    }

    private void handleAddingVisitor(Visitor visitor) {
        Task<Visitor> addVisitorTask = new Task<>() {
            @Override
            protected Visitor call() throws Exception {
                VisitorRepository repository = new VisitorRepository(datasource.getEntityManager());
                return repository.save(visitor).orElseThrow(IllegalAccessError::new);
            }
        };

        addVisitorTask.setOnFailed(workerStateEvent -> {
            Log.error(getClass().getSimpleName(), "handleAddVisitorOperation", workerStateEvent.getSource().getException());
            AlertHelper.showErrorAlert("Adding Visitor", "Adding Visitor Failed");
        });

        addVisitorTask.setOnSucceeded(workerStateEvent -> {
            AlertHelper.showInformationAlert("Adding Visitor", "Visitor Added Successfully");
            closeStage();
        });

        executor.execute(addVisitorTask);
    }

    public void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
