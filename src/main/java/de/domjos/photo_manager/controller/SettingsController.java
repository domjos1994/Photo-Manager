package de.domjos.photo_manager.controller;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.InitializationHelper;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private MainController mainController;

    private @FXML Button cmdSettingsSave, cmdSettingsHome, cmdSettingsPath;
    private @FXML CheckBox chkSettingsDebugMode;
    private @FXML TextField txtSettingsPath;

    private @FXML TextField txtSettingsTinyKey, txtSettingsTinyFile;

    public void initialize(URL location, ResourceBundle resources) {
        this.txtSettingsPath.setText(PhotoManager.GLOBALS.getSetting(Globals.PATH, "").toString());
        this.cmdSettingsPath.setOnAction(event -> {
            this.mainController.setMessage(resources.getString("settings.general.move"));
            List<File> files = Dialogs.printFileChooser(resources.getString("main.initialize.path"), true, true, false, null);
            if(files!=null) {
                if(!files.isEmpty()) {
                    Task<Void> projectTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            Platform.runLater(()->PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.WAIT));
                            updateProgress(0, 1);
                            updateProgress(0.1, 1);
                            String oldPath = PhotoManager.GLOBALS.getSetting(Globals.PATH, "").toString();
                            String path = files.get(0).getAbsolutePath();
                            String key = Globals.PATH;
                            String hidden = InitializationHelper.HIDDEN_PROJECT_DIR;
                            File file = new File(path);
                            updateProgress(0.2, 1);
                            if(!file.exists()) {
                                if(!file.mkdirs()) {
                                    Platform.exit();
                                }
                            }
                            PhotoManager.GLOBALS.saveSetting(key, path);

                            File oldDirectory = new File(oldPath + File.separatorChar + hidden);
                            FileUtils.moveDirectory(oldDirectory, new File(file.getAbsolutePath() + File.separatorChar + hidden));


                            updateProgress(1, 1);
                            return null;
                        }
                    };
                    this.mainController.getProgressBar().progressProperty().bind(projectTask.progressProperty());
                    projectTask.setOnSucceeded(event1 -> Platform.runLater(()->{
                        try {
                            this.mainController.getProgressBar().progressProperty().unbind();
                            PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT);
                            PhotoManager.GLOBALS.getStage().close();
                            new PhotoManager().start(PhotoManager.GLOBALS.getStage());
                        } catch (Exception ex) {
                            Dialogs.printException(ex);
                        }
                    }));
                    projectTask.setOnFailed(projectTask.getOnSucceeded());
                    new Thread(projectTask).start();
                }
            }
        });

        this.cmdSettingsHome.setOnAction(event -> this.mainController.back());

        this.cmdSettingsSave.setOnAction(event -> {
            PhotoManager.GLOBALS.saveSetting(Globals.TINY_KEY, this.txtSettingsTinyKey.getText());
            PhotoManager.GLOBALS.saveSetting(Globals.TINY_FILE, this.txtSettingsTinyFile.getText());
            PhotoManager.GLOBALS.setDebugMode(this.chkSettingsDebugMode.isSelected());
            if(this.chkSettingsDebugMode.isSelected()) {
                PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader() + " - (Debug)");
            } else {
                PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader());
            }
            mainController.initTinify();
        });
    }

    void init(MainController mainController) {
        this.mainController = mainController;
    }
}
