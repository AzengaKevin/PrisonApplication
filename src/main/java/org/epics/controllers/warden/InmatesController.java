package org.epics.controllers.warden;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableView<InmateItem> inmatesTable;
    @FXML
    private TableColumn<InmateItem, Integer> idCol;
    @FXML
    private TableColumn<InmateItem, String> nameCol;
    @FXML
    private TableColumn<InmateItem, String> dateOfBirthCol;
    @FXML
    private TableColumn<InmateItem, String> caseNumberCol;
    @FXML
    private TableColumn<InmateItem, String> convictionDateCol;
    @FXML
    private TableColumn<InmateItem, String> releaseDateCol;

    private static final DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    private Executor executor;
    private Datasource datasource;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });

        datasource = Datasource.getInstance();

        bindTableColumns();

        retrieveAndShowInmates();
    }

    private void bindTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        caseNumberCol.setCellValueFactory(new PropertyValueFactory<>("caseNumber"));
        dateOfBirthCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        convictionDateCol.setCellValueFactory(new PropertyValueFactory<>("convictionDate"));
        releaseDateCol.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
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
            AlertHelper.showErrorAlert("Retrieving Inmates", "A fatal error, occurred check the logs");
            Log.error(getClass().getSimpleName(), "retrieveAndShowInmates", retrieveInmatesTask.getException());
        });

        retrieveInmatesTask.setOnSucceeded(workerStateEvent -> {

            List<InmateEntity> inmateEntityList = (List<InmateEntity>) workerStateEvent.getSource().getValue();

            List<InmateItem> inmateList = inmateEntityList.stream().map(InmateItem::new).toList();

            inmatesTable.setItems(FXCollections.observableArrayList(inmateList));
        });

        executor.execute(retrieveInmatesTask);
    }

    public static class InmateItem {

        final private SimpleIntegerProperty id;
        final private SimpleStringProperty name;
        final private SimpleStringProperty caseNumber;
        final private SimpleStringProperty dateOfBirth;
        final private SimpleStringProperty convictionDate;
        final private SimpleStringProperty releaseDate;

        public InmateItem(InmateEntity inmateEntity) {
            this.id = new SimpleIntegerProperty(inmateEntity.getId());
            this.name = new SimpleStringProperty(inmateEntity.getName());
            this.caseNumber = new SimpleStringProperty(inmateEntity.getCaseNumber());
            this.dateOfBirth = new SimpleStringProperty(dateFormatter.format(inmateEntity.getDateOfBirth()));
            this.convictionDate = new SimpleStringProperty(dateFormatter.format(inmateEntity.getConvictionDate()));
            this.releaseDate = new SimpleStringProperty(dateFormatter.format(inmateEntity.getReleaseDate()));
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
