package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.controller.MainController;
import de.domjos.photo_manager.helper.ContextHelp;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class ParentController implements Initializable {
    protected MainController mainController;
    private ContextHelp contextHelp;
    protected ResourceBundle lang;

    public void initialize(URL location, ResourceBundle resources) {
        this.contextHelp = new ContextHelp();
        this.lang = resources;
        this.initContextHelp();
        this.initialize(resources);
    }

    public abstract void initialize(ResourceBundle resources);

    public void init(MainController mainController) {
        this.mainController = mainController;
    }

    protected void initContextHelp() {

    }

    protected void addContextHelp(Control control, String key) {
        this.contextHelp.add(control, key);
        control.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.F1) {
                this.contextHelp.fireMessage(control);
            }
        });
    }
}
