package de.domjos.photo_manager.controller;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class ApiController implements Initializable {
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    void init(MainController mainController) {
        this.mainController = mainController;
    }
}
