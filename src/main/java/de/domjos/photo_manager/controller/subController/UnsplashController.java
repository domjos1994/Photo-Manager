package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.services.UnsplashTask;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.controlsfx.control.PopOver;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class UnsplashController extends ParentController {
    private @FXML TitledPane pnlUnsplash;
    private @FXML TextField txtSearch;
    private @FXML Button cmdSearch, cmdPrevious, cmdNext;
    private @FXML Label lblPage;
    private @FXML ListView<Image> lvUnsplash;
    private final PopOver popOver = new PopOver();

    public ListView<Image> getUnsplashListView() {
        return this.lvUnsplash;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.pnlUnsplash.setVisible(!PhotoManager.GLOBALS.getSetting(Globals.UNSPLASH_KEY, "").equals(""));

        this.lvUnsplash.getSelectionModel().selectedItemProperty().addListener((observableValue, image, t1) -> {
            this.popOver.setArrowLocation(PopOver.ArrowLocation.RIGHT_CENTER);
            this.popOver.setHideOnEscape(true);
            this.popOver.setAutoHide(true);
            ImageView imageView = new ImageView();
            imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(t1.getThumbnail())));
            this.popOver.setContentNode(imageView);
            this.popOver.show(this.lvUnsplash);
        });

        this.cmdSearch.setOnAction(event -> {
            lblPage.setText("1");
            executeUnsplashTask(1);
        });

        this.cmdNext.setOnAction(actionEvent -> {
            int current = Integer.parseInt(lblPage.getText());
            current++;
            executeUnsplashTask(current);
            lblPage.setText(String.valueOf(current));
        });

        this.cmdPrevious.setOnAction(actionEvent -> {
            int current = Integer.parseInt(lblPage.getText());
            current--;
            executeUnsplashTask(current);
            lblPage.setText(String.valueOf(current));
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
