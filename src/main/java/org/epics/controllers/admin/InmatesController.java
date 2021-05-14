package org.epics.controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.epics.data.Datasource;
import org.epics.data.entities.InmateEntity;
import org.epics.data.repositories.InmateRepository;
import org.epics.helpers.AlertHelper;
import org.epics.helpers.Log;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InmatesController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button addInmateButton;

    @FXML
    private ProgressIndicator inmateProgressIndicator;
    @FXML
    private TableView<Inmate> inmatesTable;
    @FXML
    private TableColumn<Inmate, Integer> idCol;
    @FXML
    private TableColumn<Inmate, String> nameCol;
    @FXML
    private TableColumn<Inmate, String> caseNumberCol;
    @FXML
    private TableColumn<Inmate, String> convictionDateCol;
    @FXML
    private TableColumn<Inmate, String> releaseDateCol;
    @FXML
    private TableColumn<Inmate, String> dateOfBirthCol;

    private Executor executor;
    private Datasource datasource;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        inmateProgressIndicator.setVisible(false);

        bindTableColumns();

        executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        datasource = Datasource.getInstance();

        retrieveAndShowInmates();

        addInmateButton.setOnAction(event -> launchAddInmateStage());
    }

    private void retrieveAndShowInmates() {
        Task<List<InmateEntity>> retrieveInmatesTask = new Task<>() {
            @Override
            protected List<InmateEntity> call() throws Exception {

                InmateRepository inmateRepository = new InmateRepository(datasource.getEntityManager());
                return inmateRepository.findAll();
            }
        };

        retrieveInmatesTask.setOnFailed(workerStateEvent -> {
            inmateProgressIndicator.setVisible(false);
            AlertHelper.showErrorAlert("Retrieving Inmates", "A fatal error, occurred check the logs");
            Log.error(getClass().getSimpleName(), "retrieveAndShowInmates", retrieveInmatesTask.getException());
        });

        retrieveInmatesTask.setOnSucceeded(workerStateEvent -> {
            inmateProgressIndicator.setVisible(false);

            List<InmateEntity> inmateEntityList = (List<InmateEntity>) workerStateEvent.getSource().getValue();

            List<Inmate> inmateList = inmateEntityList.stream().map(
                    inmateEntity -> new Inmate(
                            inmateEntity.getId(),
                            inmateEntity.getName(),
                            inmateEntity.getCaseNumber(),
                            dateFormat.format(inmateEntity.getDateOfBirth()),
                            dateFormat.format(inmateEntity.getConvictionDate()),
                            dateFormat.format(inmateEntity.getReleaseDate())
                    )
            ).toList();

            inmatesTable.setItems(FXCollections.observableArrayList(inmateList));
        });

        inmateProgressIndicator.setVisible(true);
        executor.execute(retrieveInmatesTask);
    }

    private void bindTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        caseNumberCol.setCellValueFactory(new PropertyValueFactory<>("caseNumber"));
        dateOfBirthCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        convictionDateCol.setCellValueFactory(new PropertyValueFactory<>("convictionDate"));
        releaseDateCol.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
    }


    private void launchAddInmateStage() {
        try {

            Parent parent = FXMLLoader.load(getClass().getResource("/layouts/admin/AddInmate.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Add Inmate | Prison Management Software");
            Scene scene = new Scene(parent);

            scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());

            stage.setScene(scene);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(rootPane.getScene().getWindow());

            stage.setOnCloseRequest(event -> retrieveAndShowInmates());

            stage.show();

        } catch (Exception exception) {
            Log.error(getClass().getSimpleName(), "launchAddStaffStage", exception);
        }
    }

    public static class Inmate {

        final private SimpleIntegerProperty id;
        final private SimpleStringProperty name;
        final private SimpleStringProperty caseNumber;
        final private SimpleStringProperty dateOfBirth;
        final private SimpleStringProperty convictionDate;
        final private SimpleStringProperty releaseDate;

        public Inmate(int id, String name, String caseNumber, String dateOfBirth, String convictionDate, String releaseDate) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.caseNumber = new SimpleStringProperty(caseNumber);
            this.dateOfBirth = new SimpleStringProperty(dateOfBirth);
            this.convictionDate = new SimpleStringProperty(convictionDate);
            this.releaseDate = new SimpleStringProperty(releaseDate);
        }


        public int getId() {
            return id.get();
        }

        public String getName() {
            return name.get();
        }

        public String getCaseNumber() {
            return caseNumber.get();
        }

        public String getDateOfBirth() {
            return dateOfBirth.get();
        }

        public String getConvictionDate() {
            return convictionDate.get();
        }

        public String getReleaseDate() {
            return releaseDate.get();
        }

        public SimpleStringProperty releaseDateProperty() {
            return releaseDate;
        }
    }
}
