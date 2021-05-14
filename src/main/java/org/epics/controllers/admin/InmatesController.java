package org.epics.controllers.admin;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class InmatesController implements Initializable {

    public AnchorPane rootPane;
    public Button addInmateButton;
    public ProgressIndicator inmateProgressIndicator;
    public TableView inmatesTable;
    public TableColumn idCol;
    public TableColumn nameCol;
    public TableColumn caseNumberCol;
    public TableColumn ageCol;
    public TableColumn convictionDateCol;
    public TableColumn releaseDateCol;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
