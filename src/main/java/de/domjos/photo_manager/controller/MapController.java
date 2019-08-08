package de.domjos.photo_manager.controller;

import com.lynden.gmapsfx.GoogleMapView;
import de.domjos.photo_manager.helper.MapHelper;
import de.domjos.photo_manager.services.MapTask;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class MapController implements Initializable {
    private MainController mainController;


    private @FXML GoogleMapView gvMap;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    void init() {
        MapTask mapTask = new MapTask(this.mainController.getProgressBar(), null);
        MapHelper mapHelper = new MapHelper(this.gvMap, null);
        mapTask.onFinish(()->{
            try{
                mapHelper.init(mapTask.get());
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
        new Thread(mapTask).start();
    }

    void init(MainController mainController) {
        this.mainController = mainController;
    }
}
