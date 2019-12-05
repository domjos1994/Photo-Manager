package de.domjos.photo_manager.controller.mainController;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.helper.MapHelper;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.util.Collections;

public class MetaData {
    private TextField txtDate;
    private TextField txtLong, txtLat;
    private TextField txtDpiX, txtDpiY, txtPxX, txtPxY;
    private TextField txtIso, txtAperture, txtExposureTime, txtSoftware, txtCamera;
    private MapView gvLocation;
    private Image image;

    public MetaData(
            TextField txtDate, TextField txtLong, TextField txtLat, TextField txtDpiX, TextField txtDpiY, TextField txtPxX, TextField txtPxY,
            TextField txtIso, TextField txtAperture, TextField txtExposureTime, TextField txtSoftware, TextField txtCamera, MapView gvLocation) {

        this.txtDate = txtDate;
        this.txtLong = txtLong;
        this.txtLat = txtLat;
        this.txtDpiX = txtDpiX;
        this.txtDpiY = txtDpiY;
        this.txtPxX = txtPxX;
        this.txtPxY = txtPxY;
        this.txtIso = txtIso;
        this.txtAperture = txtAperture;
        this.txtExposureTime = txtExposureTime;
        this.txtSoftware = txtSoftware;
        this.txtCamera = txtCamera;
        this.gvLocation = gvLocation;
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
                            this.gvLocation.setVisible(true);

                            MapHelper mapHelper = new MapHelper(this.gvLocation, new MapPoint(metaData.getLatitude(), metaData.getLongitude()));
                            mapHelper.init(Collections.singletonList(image));
                        } else {
                            this.gvLocation.setVisible(false);
                        }
                    } catch (Exception ex) {
                        this.gvLocation.setVisible(false);
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
                    this.gvLocation.setVisible(false);
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
