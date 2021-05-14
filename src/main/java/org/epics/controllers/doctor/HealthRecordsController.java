package org.epics.controllers.doctor;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class HealthRecordsController implements Initializable {

    public TextField searchNameField;
    public Button searchUserButton;

    public TableView usersTable;
    public TableColumn nameCol;

    public Button addHealthRecordButton;

    public TableColumn diseaseCol;
    public TableColumn diagnosisDateCol;
    public TableColumn endDateCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public static class User {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty group;

        public User(int id, String name, String group) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.group = new SimpleStringProperty(group);
        }

        public int getId() {
            return id.get();
        }

        public String getName() {
            return name.get();
        }


        public String getGroup() {
            return group.get();
        }
    }

    public static class HealthRecord {

        private SimpleStringProperty disease;
        private SimpleStringProperty diagnosisDate;
        private SimpleStringProperty endDate;

        public HealthRecord(String disease, String diagnosisDate, String endDate) {

            this.disease = new SimpleStringProperty(disease);
            this.diagnosisDate = new SimpleStringProperty(diagnosisDate);
            this.endDate = new SimpleStringProperty(endDate);

        }
    }
}
