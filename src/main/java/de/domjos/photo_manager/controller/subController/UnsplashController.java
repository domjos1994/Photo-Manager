package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.services.UnsplashTask;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.PopOver;

import java.io.ByteArrayInputStream;
import java.util.ResourceBundle;

public class UnsplashController extends ParentController {
    private @FXML TitledPane pnlUnsplash;
    private @FXML TextField txtSearch;
    private @FXML Button cmdSearch, cmdPrevious, cmdNext;
    private @FXML Label lblPage;
    private @FXML ListView<Image> lvUnsplash;
    private final PopOver popOver = new PopOver();
    private int page = 1;

    public ListView<Image> getUnsplashListView() {
        return this.lvUnsplash;
    }

    @Override
    public void initialize(ResourceBundle resources) {
        this.pnlUnsplash.setDisable(PhotoManager.GLOBALS.getSetting(Globals.UNSPLASH_KEY, "").equals(""));
        this.lblPage.setText(String.format(this.lang.getString("main.image.unsplash.page"), this.page, this.page));
        this.lblPage.setTextAlignment(TextAlignment.CENTER);
        this.lblPage.setAlignment(Pos.CENTER);

        this.lvUnsplash.getSelectionModel().selectedItemProperty().addListener((observableValue, image, t1) -> {
            if(t1!=null) {
                this.popOver.setArrowLocation(PopOver.ArrowLocation.RIGHT_CENTER);
                this.popOver.setHideOnEscape(true);
                this.popOver.setAutoHide(true);
                ImageView imageView = new ImageView();
                imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(t1.getThumbnail())));
                this.popOver.setContentNode(imageView);
                this.popOver.show(this.lvUnsplash);
            }
        });

        this.cmdSearch.setOnAction(event -> {
            this.lblPage.setText(String.format(this.lang.getString("main.image.unsplash.page"), this.page, this.page));
            executeUnsplashTask(1);
        });

        this.cmdNext.setOnAction(actionEvent -> {
            this.page++;
            executeUnsplashTask(this.page);
        });

        this.cmdPrevious.setOnAction(actionEvent -> {
            this.page--;
            executeUnsplashTask(this.page);
        });

        this.lvUnsplash.setOnDragDetected(mouseEvent -> {
            Dragboard db = this.lvUnsplash.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            StringBuilder strContent = new StringBuilder();
            for(int index : this.lvUnsplash.getSelectionModel().getSelectedIndices()) {
                strContent.append(index);
                strContent.append(";");
            }
            content.putString(strContent.toString());
            db.setContent(content);
            mouseEvent.consume();
        });

        this.txtSearch.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                this.cmdSearch.fire();
            }
        });
    }

    private void executeUnsplashTask(int page) {
        String query = this.txtSearch.getText();
        UnsplashTask unsplashTask = new UnsplashTask(this.mainController.getProgressBar(), this.mainController.getMessages(), query, page);
        unsplashTask.setOnSucceeded(evt ->
                Platform.runLater(() -> {
                    try {
                        this.mainController.getProgressBar().getScene().setCursor(Cursor.DEFAULT);
                        this.lvUnsplash.getItems().clear();
                        this.lvUnsplash.getItems().addAll(unsplashTask.get());
                        this.lblPage.setText(String.format(this.lang.getString("main.image.unsplash.page"), page, unsplashTask.getMaxPages()));
                    } catch (Exception e) {
                        Dialogs.printException(e);
                    }
                })
        );
        unsplashTask.setOnFailed(unsplashTask.getOnSucceeded());
        unsplashTask.setOnCancelled(unsplashTask.getOnSucceeded());
        this.mainController.getProgressBar().getScene().setCursor(Cursor.WAIT);
        new Thread(unsplashTask).start();
    }
}
