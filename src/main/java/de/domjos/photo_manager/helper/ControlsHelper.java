package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.MainController;
import de.domjos.photo_manager.controller.subController.ParentController;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ControlsHelper {

    public static <T> void addColumnsToTable(TableView<T> tableView, List<String[]> columns) {
        for(String[] column : columns) {
            TableColumn<T, String> stringTableColumn = new TableColumn<>(column[0]);
            stringTableColumn.setText(PhotoManager.GLOBALS.getLanguage().getString(column[1]));
            stringTableColumn.setCellValueFactory(new PropertyValueFactory<>(column[2]));
            tableView.getColumns().add(stringTableColumn);
        }
    }

    public static void initController(List<ParentController> controllers, MainController mainController) {
        controllers.forEach(controller -> controller.init(mainController));
    }

    public static void unbind(Label lbl, ProgressBar pb) {
        if(lbl.textProperty().isBound()) {
            lbl.textProperty().unbind();
        }
        if(pb.progressProperty().isBound()) {
            pb.progressProperty().unbind();
        }
    }
}
