package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.services.InstagramTask;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.*;

public class InstagramController extends ParentController {
    private Image image;

    private @FXML TitledPane pnlInstagram;
    private @FXML TextField txtSearch;
    private @FXML Button cmdSearch, cmdPrevious, cmdNext;
    private @FXML Label lblPage, lblUpload;
    private @FXML ListView<Image> lvInstagram;

    private String user, pwd;
    private int page = 1;

    @Override
    public void initialize(ResourceBundle resources) {
        this.hide();

        this.lblUpload.setOnDragOver(event -> {
            if (event.getGestureSource() != this.lblUpload && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        this.lblUpload.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                this.uploadImages(this.image);
                this.image = null;
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });



        this.cmdSearch.setOnAction(event -> this.executeInstagramTask(this.page));

        this.cmdPrevious.setOnAction(event -> {
            if(this.page != 1) {
                this.page--;
            }
            this.executeInstagramTask(this.page);
        });

        this.cmdNext.setOnAction(event -> {
            if(this.lvInstagram.getItems().size() == 50) {
                this.page++;
            }
            this.executeInstagramTask(this.page);
        });
    }

    public ListView<Image> getInstagramListView() {
        return this.lvInstagram;
    }

    public void setImage(Image img) {
        this.image = img;
    }

    public void hide() {
        this.user = PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_USER, "");
        this.pwd = PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_PWD, "");
        this.pnlInstagram.setDisable((this.user.trim().isEmpty() && this.pwd.trim().isEmpty()));
    }

    private void executeInstagramTask(int page) {
        this.lblPage.setText((this.page - 1) * 50 + " - " + this.page * 50);
        String query = this.txtSearch.getText();
        InstagramTask instagramTask = new InstagramTask(this.mainController.getProgressBar(), this.mainController.getMessages(), this.user, this.pwd, query, page);
        instagramTask.setOnSucceeded(evt ->
                Platform.runLater(() -> {
                    try {
                        this.mainController.getProgressBar().getScene().setCursor(Cursor.DEFAULT);
                        this.lvInstagram.getItems().clear();
                        this.lvInstagram.getItems().addAll(instagramTask.get());
                    } catch (Exception e) {
                        Dialogs.printException(e);
                    }
                })
        );
        instagramTask.setOnFailed(instagramTask.getOnSucceeded());
        instagramTask.setOnCancelled(instagramTask.getOnSucceeded());
        this.mainController.getProgressBar().getScene().setCursor(Cursor.WAIT);
        new Thread(instagramTask).start();
    }

    private void uploadImages(Image image) {
        InstagramTask instagramTask = new InstagramTask(this.mainController.getProgressBar(), this.mainController.getMessages(), this.user, this.pwd, image);
        instagramTask.setOnSucceeded(evt ->
                Platform.runLater(() -> {
                    try {
                        this.mainController.getProgressBar().getScene().setCursor(Cursor.DEFAULT);
                    } catch (Exception e) {
                        Dialogs.printException(e);
                    }
                })
        );
        instagramTask.setOnFailed(instagramTask.getOnSucceeded());
        instagramTask.setOnCancelled(instagramTask.getOnSucceeded());
        this.mainController.getProgressBar().getScene().setCursor(Cursor.WAIT);
        new Thread(instagramTask).start();
    }
}
