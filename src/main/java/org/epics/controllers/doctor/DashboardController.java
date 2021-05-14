package org.epics.controllers.doctor;

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

    public Button healthRecordsButton;
    public Button usersButton;
    public AnchorPane rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersButton.setOnAction(event -> launchStage("/layouts/doctor/Users.fxml", "Users | Prison Management Software"));
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
