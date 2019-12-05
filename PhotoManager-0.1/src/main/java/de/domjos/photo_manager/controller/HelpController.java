package de.domjos.photo_manager.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class HelpController implements Initializable {
    private MainController mainController;

    private @FXML Button cmdHelpHome;
    private @FXML WebView wvHelp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.loadContent();

        this.cmdHelpHome.setOnAction(actionEvent -> this.mainController.back());
    }

    private void loadContent() {
        Locale locale = Locale.getDefault();
        String urlParam = "en";
        if(locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
            urlParam = "de";
        }

        WebEngine webEngine = this.wvHelp.getEngine();
        webEngine.load(HelpController.class.getResource("/help/" + urlParam + "/Einfuhrung.html").toExternalForm());
    }

    void init(MainController mainController) {
        this.mainController = mainController;
    }
}
