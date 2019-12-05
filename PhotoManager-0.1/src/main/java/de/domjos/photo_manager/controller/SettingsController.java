package de.domjos.photo_manager.controller;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.InitializationHelper;
import de.domjos.photo_manager.services.WebDav;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
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

    private @FXML TextField txtSettingsUnsplashKey;
    private @FXML PasswordField txtSettingsUnsplashSecretKey;

    private @FXML TextField txtSettingsCloudPath, txtSettingsCloudUserName;
    private @FXML PasswordField txtSettingsCloudPassword;
    private @FXML Button cmdSettingsCloudTest;

    public void initialize(URL location, ResourceBundle resources) {
        this.txtSettingsPath.setText(PhotoManager.GLOBALS.getSetting(Globals.PATH, ""));
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
                            String oldPath = PhotoManager.GLOBALS.getSetting(Globals.PATH, "");
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
                            PhotoManager.GLOBALS.saveSetting(key, path, false);

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
            PhotoManager.GLOBALS.saveSetting(Globals.DEBUG, this.chkSettingsDebugMode.isSelected(), false);
            PhotoManager.GLOBALS.saveSetting(Globals.TINY_KEY, this.txtSettingsTinyKey.getText(), true);
            PhotoManager.GLOBALS.saveSetting(Globals.TINY_FILE, this.txtSettingsTinyFile.getText(), false);
            PhotoManager.GLOBALS.saveSetting(Globals.UNSPLASH_KEY, this.txtSettingsUnsplashKey.getText(), true);
            PhotoManager.GLOBALS.saveSetting(Globals.UNSPLASH_SECRET_KEY, this.txtSettingsUnsplashSecretKey.getText(), true);
            PhotoManager.GLOBALS.saveSetting(Globals.CLOUD_PATH, this.txtSettingsCloudPath.getText(), true);
            PhotoManager.GLOBALS.saveSetting(Globals.CLOUD_USER, this.txtSettingsCloudUserName.getText(), true);
            PhotoManager.GLOBALS.saveSetting(Globals.CLOUD_PWD, this.txtSettingsCloudPassword.getText(), true);

            PhotoManager.GLOBALS.setDebugMode(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false));
            if(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false)) {
                PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader() + " - (Debug)");
            } else {
                PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader());
            }
            mainController.initTinify();
        });

        this.cmdSettingsCloudTest.setOnAction(event -> {
            WebDav webDav = new WebDav(this.txtSettingsCloudUserName.getText(), this.txtSettingsCloudPassword.getText(), this.txtSettingsCloudPath.getText());

            Color color = webDav.testConnection() ? Color.GREEN : Color.RED;
            Background background = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
            this.cmdSettingsCloudTest.setBackground(background);
        });
    }

    private void fillData() {
        this.chkSettingsDebugMode.setSelected(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false));
        this.txtSettingsTinyKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.TINY_KEY, ""));
        this.txtSettingsTinyFile.setText(PhotoManager.GLOBALS.getSetting(Globals.TINY_FILE, ""));
        this.txtSettingsUnsplashKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_KEY, ""));
        this.txtSettingsUnsplashSecretKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_SECRET_KEY, ""));
        this.txtSettingsCloudPath.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PATH, ""));
        this.txtSettingsCloudUserName.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_USER, ""));
        this.txtSettingsCloudPassword.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PWD, ""));
    }

    void init(MainController mainController) {
        this.mainController = mainController;
        this.fillData();
    }
}
