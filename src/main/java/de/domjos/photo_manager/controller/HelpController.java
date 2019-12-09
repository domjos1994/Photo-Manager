package de.domjos.photo_manager.controller;

import de.domjos.photo_manager.controller.subController.ParentController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.Locale;
import java.util.ResourceBundle;

public class HelpController extends ParentController {
    private @FXML Button cmdHelpHome;
    private @FXML WebView wvHelp;

    @Override
    public void initialize(ResourceBundle resources) {
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
}
