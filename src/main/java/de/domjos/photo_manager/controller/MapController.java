package de.domjos.photo_manager.controller;

import com.gluonhq.maps.MapView;
import de.domjos.photo_manager.controller.subController.ParentController;
import de.domjos.photo_manager.helper.MapHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.services.MapTask;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.ResourceBundle;

public class MapController extends ParentController {
    private @FXML MapView gvMap;
    private @FXML Button cmdMapHome;

    @Override
    public void initialize(ResourceBundle resources) {
        this.cmdMapHome.setOnAction(actionEvent -> mainController.back());
    }

    public void initMap(Directory directory) {
        MapTask mapTask = new MapTask(this.mainController.getProgressBar(), null, directory);
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
}
