package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.services.InstagramTask;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class InstagramController extends ParentController {
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
            if (event.getGestureSource() != this.lblUpload && (event.getDragboard().hasFiles() || event.getDragboard().hasImage())) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        this.lblUpload.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if(db.hasImage() || db.hasFiles()) {
                String path = null;
                if(db.hasImage()) {
                    try {
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(db.getImage(), null);
                        File file = new File("tmp.jpg");
                        if(file.createNewFile()) {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            IOUtils.write(ImageHelper.imageToByteArray(bufferedImage), fileOutputStream);
                            fileOutputStream.close();

                            path = file.getAbsolutePath();
                            if(file.exists()) {
                                file.deleteOnExit();
                            }
                        }
                    } catch (Exception ignored) {}
                } else {
                    List<File> files = db.getFiles();
                    if(!files.isEmpty()) {
                        if(files.size() == 1) {
                            try {
                                if(files.get(0).exists()) {
                                    path = files.get(0).getAbsolutePath();
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }

                if(path != null) {
                    Image image = new Image();
                    image.setPath(path);
                    image.setTitle(new File(path).getName());
                    this.uploadImages(image);
                    success = true;
                }
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

    public void hide() {
        this.user = PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_USER, "");
        this.pwd = PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_PWD, "");
        this.pnlInstagram.setDisable(this.user.trim().isEmpty() || this.pwd.trim().isEmpty());
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
