package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.controller.MainController;
import javafx.fxml.Initializable;

public abstract class ParentController implements Initializable {
    protected MainController mainController;

    public void init(MainController mainController) {
        this.mainController = mainController;
    }
}
