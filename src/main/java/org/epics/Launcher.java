package org.epics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent parent = FXMLLoader.load(getClass().getResource("/Login.fxml"));

        stage.setTitle("Prison Management System");

        stage.setScene(new Scene(parent));

        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
