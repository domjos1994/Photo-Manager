package de.domjos.photo_manager.controller;

import de.domjos.photo_manager.controller.subController.ParentController;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowController extends ParentController {
    private List<Image> images;
    private int counter;

    private @FXML ToolBar toolbar;
    private @FXML Button cmdSlideshowHome, cmdSlideshowNext, cmdSlideshowPrevious;
    private @FXML Button cmdSlideshowPlay, cmdSlideshowStop;
    private @FXML TextField txtSlideshowLength;
    private @FXML ImageView ivSlideshow;
    private @FXML CheckBox chkSlideshowFullscreen;
    private boolean hasListener = false;

    private Timer timer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initBindings();

        this.cmdSlideshowHome.setOnAction(actionEvent -> this.mainController.back());

        this.cmdSlideshowPrevious.setOnAction(actionEvent -> {
            this.counter--;
            this.showImage();
        });

        this.cmdSlideshowNext.setOnAction(actionEvent -> {
            this.counter++;
            this.showImage();
        });

        this.cmdSlideshowPlay.setOnAction(actionEvent -> {
            int time;
            try {
                time = Integer.parseInt(this.txtSlideshowLength.getText());
            } catch (Exception ex) {
                time = 5;
            }
            this.timer = new Timer();
            this.timer.schedule(this.initTask(), 0, 3000 + (time * 1000));

            this.cmdSlideshowPlay.setDisable(true);
            this.cmdSlideshowStop.setDisable(false);
        });

        this.cmdSlideshowStop.setOnAction(actionEvent -> {
            this.timer.cancel();

            this.cmdSlideshowPlay.setDisable(false);
            this.cmdSlideshowStop.setDisable(true);
        });

        this.chkSlideshowFullscreen.setOnAction(actionEvent -> {
            if(!this.hasListener) {
                ((Stage) this.chkSlideshowFullscreen.getScene().getWindow()).fullScreenProperty().addListener(((observableValue, aBoolean, t1) -> this.resizeControls()));
                this.hasListener = true;
            }

            boolean full = this.chkSlideshowFullscreen.isSelected();

            ((Stage) this.chkSlideshowFullscreen.getScene().getWindow()).setFullScreen(full);
        });
    }

    private TimerTask initTask() {
        return new TimerTask() {
            @Override
            public void run() {
                counter++;
                Platform.runLater(() -> showImage());
            }
        };
    }

    private void initBindings() {
        this.timer = new Timer();

        this.cmdSlideshowPrevious.disableProperty().bindBidirectional(this.cmdSlideshowNext.disableProperty());
        this.txtSlideshowLength.disableProperty().bindBidirectional(this.cmdSlideshowNext.disableProperty());
        this.cmdSlideshowNext.disableProperty().bind(this.cmdSlideshowStop.disableProperty().not());

        this.cmdSlideshowStop.setDisable(true);
    }

    void getImages(List<Image> images) {
        if(!this.ivSlideshow.fitWidthProperty().isBound()) {
            this.ivSlideshow.fitWidthProperty().bind(this.ivSlideshow.getScene().getWindow().widthProperty());
        }

        this.images = images;
        this.counter = 1;
        this.showImage();
    }

    private void showImage() {
        try {
            if(this.images.size() < this.counter) {
                this.counter = 1;
            } else if(this.counter == 0) {
                this.counter = this.images.size();
            }
            Image image = this.images.get(this.counter - 1);
            FileInputStream fis = new FileInputStream(new File(image.getPath()));
            this.ivSlideshow.setImage(new javafx.scene.image.Image(fis));
            fis.close();

            FadeTransition ft = new FadeTransition(Duration.millis(3000), this.ivSlideshow);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setCycleCount(1);
            ft.setAutoReverse(true);
            ft.play();
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void resizeControls() {
        boolean fullScreen = ((Stage) this.chkSlideshowFullscreen.getScene().getWindow()).isFullScreen();
        this.chkSlideshowFullscreen.setSelected(fullScreen);

        this.toolbar.setVisible(!fullScreen);
        AnchorPane.setTopAnchor(this.ivSlideshow, fullScreen ? -60.0 : 40.0);
        AnchorPane.setBottomAnchor(this.ivSlideshow, fullScreen ? -30.0 : 0.0);
        this.mainController.hideBars(fullScreen);
    }
}
