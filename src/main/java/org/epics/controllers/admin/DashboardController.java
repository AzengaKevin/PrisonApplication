package org.epics.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button staffButton;
    @FXML
    private Button taskManagementButton;
    @FXML
    private Button inmatesButton;
    @FXML
    private Button visitorsButton;
    @FXML
    private Button healthRecordsButton;
    @FXML
    private Button settingButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        staffButton.setOnAction(actionEvent -> launchStage("/layouts/admin/Staff.fxml", "Staff | Prison Management Software"));
        inmatesButton.setOnAction(actionEvent -> launchStage("/layouts/admin/Inmates.fxml", "Inmates | Prison Management Software"));
        visitorsButton.setOnAction(actionEvent -> launchStage("/layouts/admin/Visitors.fxml", "Visitors | Prison Management Software"));
        healthRecordsButton.setOnAction(actionEvent -> launchStage("/layouts/admin/HealthRecords.fxml", "Health Records | Prison Management Software"));
        taskManagementButton.setOnAction(actionEvent -> launchStage("/layouts/admin/TaskManagement.fxml", "Task Management | Prison Management Software"));
    }


    public void launchStage(String location, String title) {

        try {

            Parent parent = FXMLLoader.load(getClass().getResource(location));

            Stage stage = new Stage();
            stage.setTitle(title);
            Scene scene = new Scene(parent);

            scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());

            stage.setScene(scene);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(rootPane.getScene().getWindow());

            stage.show();

        } catch (Exception exception) {
            Log.error(getClass().getSimpleName(), "launchStage", exception);
        }
    }
}
