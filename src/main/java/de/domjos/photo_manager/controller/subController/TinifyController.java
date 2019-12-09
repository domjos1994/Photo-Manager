package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.services.TinifyTask;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.util.ResourceBundle;

public class TinifyController extends ParentController {
    private @FXML TitledPane pnlTinify;
    private @FXML TextField txtWidth, txtHeight;
    private @FXML Button cmdUpload;

    @Override
    public void initialize(ResourceBundle resources) {

        this.cmdUpload.setOnAction(event -> {
            try {
                TinifyTask tinifyTask = null;
                String width = this.txtWidth.getText();
                String height = this.txtHeight.getText();
                if(!this.mainController.getLvMain().getSelectionModel().isEmpty()) {
                    tinifyTask = new TinifyTask(this.mainController.getProgressBar(), this.mainController.getMessages(), width, height, this.mainController.getLvMain().getSelectionModel().getSelectedItems());
                } else {
                    if(!this.mainController.getTvMain().getSelectionModel().isEmpty()) {
                        tinifyTask = new TinifyTask(this.mainController.getProgressBar(), this.mainController.getMessages(), width, height, this.mainController.getTvMain().getSelectionModel().getSelectedItem().getValue());
                    }
                }
                assert tinifyTask != null;
                tinifyTask.onFinish(()->this.mainController.fillImageList(this.mainController.getTvMain().getSelectionModel().getSelectedItem().getValue()));
                new Thread(tinifyTask).start();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
    }

    public void initTinify() {
        this.pnlTinify.setVisible(!PhotoManager.GLOBALS.getSetting(Globals.TINY_KEY, "").equals(""));
    }
}
