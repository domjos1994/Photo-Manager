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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
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

    private @FXML CheckBox chkSettingsDirectoriesDelete;
    private @FXML TextField txtSettingsDirectoriesDelete;
    private @FXML Button cmdSettingsDirectoriesDelete;
    private @FXML TableView<DirRow> tblSettingsDirectories;

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

        this.chkSettingsDirectoriesDelete.selectedProperty().addListener(((observableValue, aBoolean, t1) -> this.cmdSettingsDirectoriesDelete.setDisable(!t1)));
        this.cmdSettingsDirectoriesDelete.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.path"));
            File file = directoryChooser.showDialog(null);
            if(file!=null) {
                this.txtSettingsDirectoriesDelete.setText(file.getAbsolutePath());
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
            if(!this.chkSettingsDirectoriesDelete.isSelected() || this.txtSettingsDirectoriesDelete.getText().trim().isEmpty()) {
                PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES_DELETE_KEY, "", false);
            } else {
                PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES_DELETE_KEY, this.txtSettingsDirectoriesDelete.getText().trim(), false);
            }
            this.saveRowsToSettings();

            PhotoManager.GLOBALS.setDebugMode(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false));
            if(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false)) {
                PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader() + " - (Debug)");
            } else {
                PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader());
            }
            mainController.initTinify();

            Dialogs.printNotification(Alert.AlertType.INFORMATION, PhotoManager.GLOBALS.getLanguage().getString("settings.saved"), PhotoManager.GLOBALS.getLanguage().getString("settings.saved.text"));
        });

        this.cmdSettingsCloudTest.setOnAction(event -> {
            WebDav webDav = new WebDav(this.txtSettingsCloudUserName.getText(), this.txtSettingsCloudPassword.getText(), this.txtSettingsCloudPath.getText());

            Color color = webDav.testConnection() ? Color.GREEN : Color.RED;
            Background background = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
            this.cmdSettingsCloudTest.setBackground(background);
        });
    }

    private void fillData() {
        this.cmdSettingsDirectoriesDelete.setDisable(true);
        this.chkSettingsDebugMode.setSelected(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false));
        this.txtSettingsTinyKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.TINY_KEY, ""));
        this.txtSettingsTinyFile.setText(PhotoManager.GLOBALS.getSetting(Globals.TINY_FILE, ""));
        this.txtSettingsUnsplashKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_KEY, ""));
        this.txtSettingsUnsplashSecretKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_SECRET_KEY, ""));
        this.txtSettingsCloudPath.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PATH, ""));
        this.txtSettingsCloudUserName.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_USER, ""));
        this.txtSettingsCloudPassword.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PWD, ""));
        SettingsController.getRowsFromSettings().forEach(this.tblSettingsDirectories.getItems()::add);

        String deleteDir = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_DELETE_KEY, "");
        this.chkSettingsDirectoriesDelete.setSelected(!deleteDir.trim().isEmpty());
        if(deleteDir.trim().isEmpty()) {
            this.txtSettingsDirectoriesDelete.setText("");
        } else {
            this.txtSettingsDirectoriesDelete.setText(deleteDir);
        }
    }

    public static List<DirRow> getRowsFromSettings() {
        List<DirRow> dirRows = new LinkedList<>();

        int index = 1;
        while(!PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_TITLE + "_" + index, "").equals("")) {
            String title = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_TITLE + "_" + index, "");
            String path = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_PATH + "_" + index, "");
            String icon = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_ICON + "_" + index, "");

            if(!path.trim().isEmpty()) {
                DirRow dirRow = new DirRow();
                dirRow.setTitle(title);
                dirRow.setPath(path);
                dirRow.setIcon(icon);
                dirRows.add(dirRow);
            }
            index++;
        }
        return dirRows;
    }

    private void saveRowsToSettings() {
        int index = 1;
        for(DirRow dirRow : this.tblSettingsDirectories.getItems()) {
            if(!dirRow.getTitle().trim().isEmpty() && !dirRow.getPath().trim().isEmpty()) {
                PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES_TITLE + "_" + index, dirRow.getTitle().trim(), false);
                PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES_PATH + "_" + index, dirRow.getPath().trim(), false);
                PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES_ICON + "_" + index, dirRow.getIcon().trim(), false);
            }
            index++;
        }
    }

    private void initDirTableView() {
        TableColumn<DirRow, String> colSettingsDirectoriesTitle = new TableColumn<>(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.title"));
        colSettingsDirectoriesTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSettingsDirectoriesTitle.setCellFactory(TextFieldTableCell.forTableColumn());
        colSettingsDirectoriesTitle.setOnEditCommit(event -> {
            String val = event.getNewValue();
            event.getRowValue().setTitle(val);
            this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());
            this.tblSettingsDirectories.getItems().add(new DirRow());
        });
        colSettingsDirectoriesTitle.setEditable(true);
        colSettingsDirectoriesTitle.setMinWidth(150);
        colSettingsDirectoriesTitle.setPrefWidth(150);

        TableColumn<DirRow, String> colSettingsDirectoriesPath = new TableColumn<>(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.path"));
        colSettingsDirectoriesPath.setCellValueFactory(new PropertyValueFactory<>("path"));
        colSettingsDirectoriesPath.setEditable(true);
        colSettingsDirectoriesPath.setOnEditStart(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.path"));
            File file = directoryChooser.showDialog(null);
            if(file!=null) {
                event.getRowValue().setPath(file.getAbsolutePath());
                this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());
            }
        });
        colSettingsDirectoriesPath.setMinWidth(350);
        colSettingsDirectoriesPath.setPrefWidth(500);

        TableColumn<DirRow, String> colSettingsDirectoriesIcon = new TableColumn<>(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.icon"));
        colSettingsDirectoriesIcon.setCellValueFactory(new PropertyValueFactory<>("icon"));
        colSettingsDirectoriesIcon.setEditable(true);
        colSettingsDirectoriesIcon.setOnEditStart(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.icon"));
            File file = directoryChooser.showDialog(null);
            if(file!=null) {
                event.getRowValue().setIcon(file.getAbsolutePath());
                this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());
            }
        });
        colSettingsDirectoriesIcon.setMinWidth(350);
        colSettingsDirectoriesIcon.setPrefWidth(500);

        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesTitle);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesPath);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesIcon);
        this.tblSettingsDirectories.getItems().add(new DirRow());
    }

    void init(MainController mainController) {
        this.mainController = mainController;
        this.fillData();
        this.initDirTableView();
    }

    public static class DirRow {
        private String title, path, icon;

        public DirRow() {
            super();
            this.title = "";
            this.path = "";
            this.icon = "";
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getIcon() {
            return this.icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}
