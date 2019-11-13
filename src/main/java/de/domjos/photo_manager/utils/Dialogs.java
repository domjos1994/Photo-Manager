package de.domjos.photo_manager.utils;

import de.domjos.photo_manager.PhotoManager;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public final class Dialogs {

    public static void printFXML(String path, ResourceBundle language, String header, boolean wait) throws Exception {
        Dialogs.printFXML(path, language, header, wait, PhotoManager.GLOBALS.getStage());
    }

    public static void printFXML(String path, ResourceBundle language, String header, boolean wait, Stage stage) throws Exception {
        Parent root = FXMLLoader.load(PhotoManager.class.getResource(path), language);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Dialogs.class.getResource("/styles/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(header);
        stage.getIcons().add(new Image(PhotoManager.class.getResourceAsStream("/images/header.png")));

        if(wait) {
            stage.showAndWait();
        } else {
            stage.show();
        }
    }

    public static void printAlert(Alert.AlertType type, String title, String header, String content) {
        if(PhotoManager.GLOBALS.isDebugMode()) {
            Dialogs.printNotification(type, title, header);
        } else {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }

    public static boolean printConfirmDialog(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type, title, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }

    public static void printException(Throwable ex) {
        PhotoManager.GLOBALS.getLogger().error("Unhandled " + ex.getClass().getName(), ex);
        if(!PhotoManager.GLOBALS.isDebugMode()) {
            Dialogs.printNotification(Alert.AlertType.ERROR, "Unhandled " + ex.getClass().getName(), ex.toString());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unhandled " + ex.getClass().getName());
            alert.setHeaderText(ex.getLocalizedMessage());
            alert.setContentText(ex.toString());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();
            Label label = new Label("The exception stacktrace was:");
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        }
    }

    public static void printNotification(Alert.AlertType alertType, String title, String message) {
        Notifications notifications = Notifications.create();
        notifications.text(title);
        notifications.text(message);
        notifications.owner(PhotoManager.GLOBALS.getStage());
        notifications.position(Pos.BASELINE_CENTER);
        switch (alertType) {
            case ERROR:
                notifications.darkStyle().showError();
                break;
            case WARNING:
                notifications.darkStyle().showWarning();
                break;
            case INFORMATION:
                notifications.darkStyle().showInformation();
                break;
            case CONFIRMATION:
                notifications.darkStyle().showConfirm();
                break;
            default:
                notifications.darkStyle().show();
        }
    }

    public static List<File> printFileChooser(String title, boolean open, boolean dir, boolean multiSelect, Map<String, String> extensions) {
        List<File> files = new LinkedList<>();
        if(dir) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(title);
            files.add(directoryChooser.showDialog(null));
            return files;
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            if(extensions!=null) {
                for(Map.Entry<String, String> extensionEntry : extensions.entrySet()) {
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionEntry.getKey(), extensionEntry.getValue()));
                }
            }
            if(open) {
                if(multiSelect) {
                    files = fileChooser.showOpenMultipleDialog(null);
                } else {
                    files.add(fileChooser.showOpenDialog(null));
                }
            } else {
                files.add(fileChooser.showSaveDialog(null));
            }
        }
        return files;
    }
}
