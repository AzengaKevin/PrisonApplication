package org.epics.controllers.taskManager;

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

    public Button tasksButton;
    public Button inmatesButton;
    public AnchorPane rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tasksButton.setOnAction(event -> launchStage("/layouts/taskManager/Tasks.fxml", "Tasks | Prison Management Software"));
        inmatesButton.setOnAction(event -> launchStage("/layouts/taskManager/Inmates.fxml", "Inmates | Prison Management Software"));
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
