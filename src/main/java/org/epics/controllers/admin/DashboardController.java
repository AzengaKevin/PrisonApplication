package org.epics.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.epics.helpers.Log;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class DashboardController implements Initializable {

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
        inmatesButton.setOnAction(actionEvent -> launchStage("/layouts/admin/Inmates.fxml", "Staff | Prison Management Software"));
    }


    public void launchStage(String location, String title) {

        try {

            Parent parent = FXMLLoader.load(getClass().getResource(location));

            Stage stage = new Stage();
            stage.setTitle(title);
            Scene scene = new Scene(parent);

            scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());

            stage.setScene(scene);

            stage.show();

        } catch (Exception exception) {
            Log.error(getClass().getSimpleName(), "launchStage", exception);
        }
    }
}
