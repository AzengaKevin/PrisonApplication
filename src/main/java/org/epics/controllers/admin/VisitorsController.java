package org.epics.controllers.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.epics.data.Datasource;
import org.epics.data.entities.Visitor;
import org.epics.data.repositories.VisitorRepository;
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

public class VisitorsController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TableView<VisitorItem> visitorsTable;
    @FXML
    private TableColumn<VisitorItem, Integer> idCol;
    @FXML
    public TableColumn<VisitorItem, String> nameCol;
    @FXML
    private TableColumn<VisitorItem, String> hudumaNumberCol;
    @FXML
    private TableColumn<VisitorItem, String> genderCol;
    @FXML
    private TableColumn<VisitorItem, String> dobCol;
    @FXML
    private TableColumn<VisitorItem, String> phoneCol;
    @FXML
    private TableColumn<VisitorItem, String> inmateField;

    public static final DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

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

        bindColumns();

        retrieveAndShowAllVisitors();
    }

    private void bindColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        hudumaNumberCol.setCellValueFactory(new PropertyValueFactory<>("hudumaNumber"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dob"));
        inmateField.setCellValueFactory(new PropertyValueFactory<>("inmate"));
    }

    private void retrieveAndShowAllVisitors() {
        Task<List<Visitor>> retrieveVisitorsTask = new Task<>() {
            @Override
            protected List<Visitor> call() throws Exception {
                VisitorRepository repository = new VisitorRepository(datasource.getEntityManager());
                return repository.findAll();
            }
        };

        retrieveVisitorsTask.setOnFailed(workerStateEvent -> {
            Log.error(getClass().getSimpleName(), "retrieveAndShowAllVisitors: failed", workerStateEvent.getSource().getException());
            AlertHelper.showInformationAlert("Get all visitors", "Failed in retrieving users, check the log");
        });

        retrieveVisitorsTask.setOnSucceeded(workerStateEvent -> {
            List<Visitor> visitorList = (List<Visitor>) workerStateEvent.getSource().getValue();
            List<VisitorItem> visitorItems = visitorList.stream().map(VisitorItem::new).toList();

            visitorsTable.setItems(FXCollections.observableArrayList(visitorItems));
        });

        executor.execute(retrieveVisitorsTask);
    }

    public static class VisitorItem {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty hudumaNumber;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty dob;
        private final SimpleStringProperty gender;
        private final SimpleStringProperty inmate;

        public VisitorItem(Visitor visitor) {
            this.id = new SimpleIntegerProperty(visitor.getId());
            this.name = new SimpleStringProperty(visitor.getName());
            this.hudumaNumber = new SimpleStringProperty(visitor.getHudumaNumber());
            this.phone = new SimpleStringProperty(visitor.getPhone());
            this.dob = new SimpleStringProperty(dateFormatter.format(visitor.getDate()));
            this.gender = new SimpleStringProperty(visitor.getGender().toString());
            this.inmate = new SimpleStringProperty(visitor.getInmateEntity().getName());
        }

        public int getId() {
            return id.get();
        }

        public String getName() {
            return name.get();
        }

        public String getHudumaNumber() {
            return hudumaNumber.get();
        }

        public String getGender() {
            return gender.get();
        }

        public String getPhone() {
            return phone.get();
        }

        public String getDob() {
            return dob.get();
        }

        public String getInmate() {
            return inmate.get();
        }
    }
}
