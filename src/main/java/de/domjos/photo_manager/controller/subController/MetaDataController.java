package de.domjos.photo_manager.controller.subController;

import com.gluonhq.maps.MapPoint;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.helper.MapHelper;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.Collections;
import java.util.ResourceBundle;

public class MetaDataController extends ParentController {
    private @FXML TextField txtDate;
    private @FXML TextField txtLong, txtLat;
    private @FXML TextField txtDpiX, txtDpiY, txtPxX, txtPxY;
    private @FXML TextField txtIso, txtAperture, txtExposureTime, txtSoftware, txtCamera;
    private Image image;

    @Override
    public void initialize(ResourceBundle resources) {
        this.setImage(null);
    }

    public void setImage(Image image) {
        this.image = image;
        new Thread(this::fillMetaData).start();
    }

    private void fillMetaData() {
        try {
            if(this.image != null) {
                // image-date
                de.domjos.photo_manager.model.gallery.MetaData metaData = ImageHelper.readMetaData(this.image.getPath());
                if(metaData.getOriginal()!=null) {
                    this.txtDate.setText(metaData.getOriginal());
                }

                // gps-data
                Platform.runLater(()->{
                    this.txtLong.setText(String.valueOf(metaData.getLongitude()));
                    this.txtLat.setText(String.valueOf(metaData.getLatitude()));
                    try {
                        if(metaData.getLatitude()!=0 && metaData.getLongitude()!=0) {
                            this.mainController.getMapView().setDisable(false);

                            MapHelper mapHelper = new MapHelper(this.mainController.getMapView(), new MapPoint(metaData.getLatitude(), metaData.getLongitude()));
                            mapHelper.init(Collections.singletonList(image));
                        } else {
                            this.mainController.getMapView().setDisable(true);
                        }
                    } catch (Exception ex) {
                        this.mainController.getMapView().setDisable(true);
                    }

                    // resolution-data
                    this.txtDpiX.setText(String.valueOf(metaData.getXResolutionInDPI()));
                    this.txtDpiY.setText(String.valueOf(metaData.getYResolutionInDPI()));
                    this.txtPxX.setText(String.valueOf(metaData.getXResolutionInPX()));
                    this.txtPxY.setText(String.valueOf(metaData.getYResolutionInPX()));

                    // exif-data
                    this.txtIso.setText(String.valueOf(metaData.getIso()));
                    this.txtAperture.setText(String.valueOf(metaData.getAperture()));
                    this.txtExposureTime.setText(String.valueOf(metaData.getExposureTime()));
                    this.txtSoftware.setText(metaData.getEditedWith());
                    this.txtCamera.setText(metaData.getCamera());
                });
            } else {
                Platform.runLater(()-> {
                    if(this.mainController != null) {
                        this.mainController.getMapView().setDisable(true);
                    }
                    this.txtDate.setText("");
                    this.txtLat.setText("");
                    this.txtLong.setText("");
                    this.txtDpiX.setText("");
                    this.txtDpiY.setText("");
                    this.txtPxX.setText("");
                    this.txtPxY.setText("");
                    this.txtIso.setText("");
                    this.txtAperture.setText("");
                    this.txtExposureTime.setText("");
                    this.txtSoftware.setText("");
                    this.txtCamera.setText("");
                });
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }
}
