package de.domjos.photo_manager.custom;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.MainController;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

@SuppressWarnings("unused")
public class ZoomImageView extends AnchorPane {
    private Image originalImage;

    private @FXML ScrollPane scroller;
    private @FXML ImageView iv;
    private @FXML Slider zoom;
    private @FXML Label lbl;
    private @FXML Button cmd100;
    private int max;

    private MainController mainController;

    public ZoomImageView() {
        this.max = PhotoManager.GLOBALS.getSetting(Globals.MAX_ZOOM_VALUE, Globals.MAX_ZOOM_VALUE_DEF);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/custom/ZoomImageView.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
            this.init();
        } catch (Exception exception) {
            Dialogs.printException(exception);
        }
    }

    public void setController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setImage(BufferedImage bufferedImage) {
        this.originalImage = SwingFXUtils.toFXImage(bufferedImage, null);
        this.iv.setImage(this.originalImage);

        this.zoom();
    }

    public void setImage(Image image) {
        this.originalImage = image;
        this.iv.setImage(this.originalImage);

        this.zoom();
    }

    public void resetImage() {
        this.originalImage = null;
        this.iv.setImage(null);
    }

    public void updateMax() {
        this.max = PhotoManager.GLOBALS.getSetting(Globals.MAX_ZOOM_VALUE, Globals.MAX_ZOOM_VALUE_DEF);
        this.zoom.setMax(this.max);
    }

    public BufferedImage getBufferedImage() {
        return SwingFXUtils.fromFXImage(this.originalImage, null);
    }

    public Image getImage() {
        return this.originalImage;
    }

    public double getZoomValue() {
        return this.zoom.getValue();
    }

    public void setZoomValue(double value) {
        this.zoom.setValue(value);
    }

    private void init() {
        this.zoom.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            this.zoom();
            this.updateLabel(this.zoom.getValue());
        });

        this.cmd100.setOnAction(event -> {
            this.zoom.setValue(100.0);
            this.zoom();
            this.updateLabel(this.zoom.getValue());
        });

        this.iv.setOnScroll(event -> {
            if(event.getDeltaY() > 0) {
                double newValue = this.zoom.getValue() + 20;
                if(newValue <= this.max) {
                    this.zoom.setValue(newValue);
                }
            } else {
                double newValue = this.zoom.getValue() - 20;
                if(newValue >= 0) {
                    this.zoom.setValue(newValue);
                }
            }
            this.zoom();
            this.updateLabel(this.zoom.getValue());
        });

        this.iv.setOnDragDetected(event -> {
            try {
                if(this.originalImage != null) {
                    Dragboard dragboard = this.iv.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                    ClipboardContent clipboardContent = new ClipboardContent();
                    if(this.mainController == null) {
                        clipboardContent.putImage(this.originalImage);

                    } else {
                        de.domjos.photo_manager.model.gallery.Image img = this.mainController.getLvMain().getSelectionModel().getSelectedItem();
                        if(img != null) {
                            File file = new File(new File(img.getPath()).getName());
                            if(file.createNewFile()) {
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                IOUtils.write(ImageHelper.imageToByteArray(SwingFXUtils.fromFXImage(this.originalImage, null)), fileOutputStream);
                                fileOutputStream.close();
                                clipboardContent.putFiles(Collections.singletonList(file));

                                if(file.exists()) {
                                    file.deleteOnExit();
                                }
                            }
                        }
                    }
                    dragboard.setContent(clipboardContent);
                    event.consume();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.iv.setFitWidth(this.scroller.getWidth());
        this.iv.setFitHeight(this.scroller.getHeight());
        this.iv.preserveRatioProperty().set(true);

        this.zoom.setMax(this.max);
        this.zoom.setValue(100);
        this.updateLabel(100.0);
        this.zoom();
    }

    private void zoom() {
        if(this.originalImage != null) {
            double value = this.zoom.getValue();
            double width = this.scroller.getWidth();
            double height = this.scroller.getHeight();

            double zoomedWidth = (width / 100) * value;
            double zoomedHeight = (height / 100) * value;
            this.iv.setFitWidth(zoomedWidth);
            this.iv.setFitHeight(zoomedHeight);
        }
    }

    private void updateLabel(double value) {
        this.lbl.setText(String.format("%2.2f", value) + " %");
    }
}
