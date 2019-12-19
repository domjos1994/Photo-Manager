package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ControlsHelper;
import de.domjos.photo_manager.model.gallery.TemporaryEdited;
import de.domjos.photo_manager.services.HistoryTask;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController extends ParentController {
    private @FXML TableView<TemporaryEdited> tblMainImageHistory;

    @Override
    public void initialize(ResourceBundle resources) {
        ControlsHelper.addColumnsToTable(this.tblMainImageHistory, Arrays.asList(
                new String[]{"ChangeType", "main.image.history.type", "changeType"},
                new String[]{"Value", "main.image.history.value", "stringValue"}
        ));

        this.tblMainImageHistory.getSelectionModel().selectedItemProperty().addListener((observableValue, temporaryEdited, t1) -> {
            if(!this.tblMainImageHistory.getSelectionModel().isEmpty()) {
                this.mainController.getEditedImage();
            }
        });
    }

    public List<TemporaryEdited> getItems() {
        return this.tblMainImageHistory.getItems();
    }

    public void selectLast() {
        if(this.tblMainImageHistory.getSelectionModel().isEmpty()) {
            this.tblMainImageHistory.getSelectionModel().select(this.tblMainImageHistory.getItems().size() - 1);
        }
    }

    public long getSelectedId() {
        return this.tblMainImageHistory.getSelectionModel().getSelectedItem().getId();
    }

    public long getSelectedIndex() {
        return this.tblMainImageHistory.getSelectionModel().getSelectedIndex();
    }

    public void reloadHistory(long id) throws Exception {
        this.tblMainImageHistory.getItems().clear();
        TemporaryEdited root = new TemporaryEdited();
        root.setChangeType(TemporaryEdited.ChangeType.None);
        this.tblMainImageHistory.getItems().add(root);
        for(TemporaryEdited temporaryEdited : PhotoManager.GLOBALS.getDatabase().getTemporaryEdited(id)) {
            this.tblMainImageHistory.getItems().add(temporaryEdited);
        }
    }
}
