package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.PhotoManager;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
}
