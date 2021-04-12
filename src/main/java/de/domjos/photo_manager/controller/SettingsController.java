package de.domjos.photo_manager.controller;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.subController.ParentController;
import de.domjos.photo_manager.helper.InitializationHelper;
import de.domjos.photo_manager.helper.Validator;
import de.domjos.photo_manager.model.gallery.BatchTemplate;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Folder;
import de.domjos.photo_manager.services.WebDav;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.beans.InvalidationListener;
import javafx.beans.value.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.util.*;

public class SettingsController extends ParentController {
    private final Validator validator = new Validator();

    private @FXML Button cmdSettingsSave, cmdSettingsHome;
    private @FXML CheckBox chkSettingsDebugMode, chkSettingsPath, chkSettingsReload;
    private @FXML TextField txtSettingsZoomFactor;

    private @FXML TextField txtSettingsTinyKey, txtSettingsTinyFile;

    private @FXML TextField txtSettingsUnsplashKey;
    private @FXML PasswordField txtSettingsUnsplashSecretKey;

    private @FXML TextField txtSettingsCloudPath, txtSettingsCloudUserName;
    private @FXML PasswordField txtSettingsCloudPassword;
    private @FXML Button cmdSettingsCloudTest;

    private @FXML TextField txtSettingsInstagramUser;
    private @FXML PasswordField txtSettingsInstagramPwd;

    private @FXML CheckBox chkSettingsDirectoriesDelete;
    private @FXML TextField txtSettingsDirectoriesDelete;
    private @FXML Button cmdSettingsDirectoriesDelete;
    private @FXML TableView<Directory> tblSettingsDirectories;

    private @FXML Accordion accSettings;

    private ResourceBundle lang;

    @Override
    public void initialize(ResourceBundle resources) {
        this.lang = PhotoManager.GLOBALS.getLanguage();
        this.accSettings.setExpandedPane(this.accSettings.getPanes().get(0));

        this.chkSettingsDirectoriesDelete.selectedProperty().addListener(((observableValue, aBoolean, t1) -> this.cmdSettingsDirectoriesDelete.setDisable(!t1)));
        this.cmdSettingsDirectoriesDelete.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(this.lang.getString("settings.directories.path"));
            File file = directoryChooser.showDialog(null);
            if(file!=null) {
                this.txtSettingsDirectoriesDelete.setText(file.getAbsolutePath());
            }
        });

        this.cmdSettingsHome.setOnAction(event -> this.mainController.back());

        this.cmdSettingsSave.setOnAction(event -> {
            if(this.validator.check()) {
                PhotoManager.GLOBALS.saveSetting(Globals.DEBUG, this.chkSettingsDebugMode.isSelected(), false);
                PhotoManager.GLOBALS.saveSetting(Globals.TITLE_PATH, this.chkSettingsPath.isSelected(), false);
                PhotoManager.GLOBALS.saveSetting(Globals.RELOAD_ON_START, this.chkSettingsReload.isSelected(), false);
                PhotoManager.GLOBALS.saveSetting(Globals.MAX_ZOOM_VALUE, Integer.parseInt(this.txtSettingsZoomFactor.getText()), false);
                PhotoManager.GLOBALS.saveSetting(Globals.TINY_KEY, this.txtSettingsTinyKey.getText(), true);
                PhotoManager.GLOBALS.saveSetting(Globals.TINY_FILE, this.txtSettingsTinyFile.getText(), false);
                PhotoManager.GLOBALS.saveSetting(Globals.UNSPLASH_KEY, this.txtSettingsUnsplashKey.getText(), true);
                PhotoManager.GLOBALS.saveSetting(Globals.UNSPLASH_SECRET_KEY, this.txtSettingsUnsplashSecretKey.getText(), true);
                PhotoManager.GLOBALS.saveSetting(Globals.CLOUD_PATH, this.txtSettingsCloudPath.getText(), true);
                PhotoManager.GLOBALS.saveSetting(Globals.CLOUD_USER, this.txtSettingsCloudUserName.getText(), true);
                PhotoManager.GLOBALS.saveSetting(Globals.CLOUD_PWD, this.txtSettingsCloudPassword.getText(), true);
                PhotoManager.GLOBALS.saveSetting(Globals.INSTAGRAM_USER, this.txtSettingsInstagramUser.getText(), true);
                PhotoManager.GLOBALS.saveSetting(Globals.INSTAGRAM_PWD, this.txtSettingsInstagramPwd.getText(), true);
                this.saveDeleteFolder();
                this.saveRowsToSettings();

                this.updateProgram();
                Dialogs.printNotification(Alert.AlertType.INFORMATION, this.lang.getString("settings.saved"), this.lang.getString("settings.saved.text"));
            } else {
                Dialogs.printNotification(Alert.AlertType.ERROR, this.lang.getString("settings.error"), this.lang.getString("settings.error.text"));
            }
        });

        this.cmdSettingsCloudTest.setOnAction(event -> {
            WebDav webDav = new WebDav(this.txtSettingsCloudUserName.getText(), this.txtSettingsCloudPassword.getText(), this.txtSettingsCloudPath.getText());

            Color color = webDav.testConnection() ? Color.GREEN : Color.RED;
            Background background = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
            this.cmdSettingsCloudTest.setBackground(background);
        });
    }

    private void updateProgram() {
        PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader());
        this.mainController.UpdateImageView();
        this.mainController.initTinify();
    }

    private void fillData() {
        this.cmdSettingsDirectoriesDelete.setDisable(true);
        this.chkSettingsDebugMode.setSelected(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false));
        this.chkSettingsPath.setSelected(PhotoManager.GLOBALS.getSetting(Globals.TITLE_PATH, false));
        this.chkSettingsReload.setSelected(PhotoManager.GLOBALS.getSetting(Globals.RELOAD_ON_START, false));
        this.txtSettingsZoomFactor.setText(String.valueOf(PhotoManager.GLOBALS.getSetting(Globals.MAX_ZOOM_VALUE, Globals.MAX_ZOOM_VALUE_DEF)));
        this.txtSettingsTinyKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.TINY_KEY, ""));
        this.txtSettingsTinyFile.setText(PhotoManager.GLOBALS.getSetting(Globals.TINY_FILE, ""));
        this.txtSettingsUnsplashKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_KEY, ""));
        this.txtSettingsUnsplashSecretKey.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_SECRET_KEY, ""));
        this.txtSettingsCloudPath.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PATH, ""));
        this.txtSettingsCloudUserName.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_USER, ""));
        this.txtSettingsCloudPassword.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PWD, ""));
        this.txtSettingsInstagramUser.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_USER, ""));
        this.txtSettingsInstagramPwd.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_PWD, ""));
        SettingsController.getRowsFromSettings().forEach(this.tblSettingsDirectories.getItems()::add);
        Directory directory = new Directory();
        directory.setFolder(new Folder());
        this.tblSettingsDirectories.getItems().add(directory);

        String deleteDir = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_DELETE_KEY, "");
        this.chkSettingsDirectoriesDelete.setSelected(!deleteDir.trim().isEmpty());
        if(deleteDir.trim().isEmpty()) {
            this.txtSettingsDirectoriesDelete.setText("");
        } else {
            this.txtSettingsDirectoriesDelete.setText(deleteDir);
        }
    }

    public static List<Directory> getRowsFromSettings() {
        List<Directory> directories = new LinkedList<>();

        try {
            directories = PhotoManager.GLOBALS.getDatabase().getDirectories("folder<>0", false);
        } catch (Exception ignored) {}

        return directories;
    }

    private void saveRowsToSettings() {
        StringBuilder stringBuilder = new StringBuilder(" folder <> 0 and id not in (");
        for(Directory directory : this.tblSettingsDirectories.getItems()) {
            if(!directory.getTitle().trim().isEmpty() && !directory.getPath().trim().isEmpty()) {
                try {
                    long id = 1;
                    if(directory.getFolder().getDirectory() != null) {
                        id = directory.getFolder().getDirectory().getId();
                    }
                    stringBuilder.append(PhotoManager.GLOBALS.getDatabase().insertOrUpdateDirectory(directory, id, false));
                    stringBuilder.append(", ");
                } catch (Exception ex) {
                    Dialogs.printException(ex);
                }
            }
        }
        String where = (stringBuilder.toString() + ")").replace(", )", ")");
        try {
            List<Directory> directories = PhotoManager.GLOBALS.getDatabase().getDirectories(where, false);
            for(Directory directory : directories) {
                PhotoManager.GLOBALS.getDatabase().deleteDirectory(directory);
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void saveDeleteFolder() {
        if(!this.chkSettingsDirectoriesDelete.isSelected() || this.txtSettingsDirectoriesDelete.getText().trim().isEmpty()) {
            PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES_DELETE_KEY, "", false);
        } else {
            PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES_DELETE_KEY, this.txtSettingsDirectoriesDelete.getText().trim(), false);
        }
    }

    private void initDirTableView() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem(this.lang.getString("sys.delete"));
        menuItem.setOnAction(event -> {
            this.tblSettingsDirectories.getItems().remove(this.tblSettingsDirectories.getSelectionModel().getSelectedIndex());
            this.saveRowsToSettings();
        });
        contextMenu.getItems().add(menuItem);
        this.tblSettingsDirectories.setContextMenu(contextMenu);

        TableColumn<Directory, String> colSettingsDirectoriesTitle = new TableColumn<>(this.lang.getString("settings.directories.title"));
        colSettingsDirectoriesTitle.setCellValueFactory(param -> returnStringValue(param.getValue().getTitle()));
        colSettingsDirectoriesTitle.setCellFactory(TextFieldTableCell.forTableColumn());
        colSettingsDirectoriesTitle.setOnEditCommit(event -> {
            String val = event.getNewValue();
            event.getRowValue().setTitle(val);
            this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());

            Directory directory = new Directory();
            directory.setFolder(new Folder());
            this.tblSettingsDirectories.getItems().add(directory);
        });
        colSettingsDirectoriesTitle.setEditable(true);

        List<Directory> directories = new LinkedList<>();
        try {
           directories = PhotoManager.GLOBALS.getDatabase().getDirectories("", true);
        } catch (Exception ignored) {}
        TableColumn<Directory, Directory> colSettingsDirectoriesParent = new TableColumn<>(this.lang.getString("settings.directories"));
        colSettingsDirectoriesParent.setCellValueFactory(param -> returnDirectoryValue(param.getValue().getFolder().getDirectory()));
        List<Directory> finalDirectories = directories;
        colSettingsDirectoriesParent.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<>() {
                 @Override
                 public String toString(Directory object) {
                     if (object != null) {
                         return object.getTitle();
                     } else {
                         return "";
                     }
                 }

                 @Override
                 public Directory fromString(String string) {
                     if (string != null) {
                         for (Directory batchTemplate : finalDirectories) {
                             if (batchTemplate != null) {
                                 if (batchTemplate.getTitle().equals(string)) {
                                     return batchTemplate;
                                 }
                             }
                         }
                     }
                     return null;
                 }
             }, FXCollections.observableList(directories)

        ));
        colSettingsDirectoriesParent.setOnEditCommit(event -> {
            Directory directory = event.getNewValue();
            event.getRowValue().getFolder().setDirectory(directory);
            this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());

            Directory dir = new Directory();
            dir.setFolder(new Folder());
            this.tblSettingsDirectories.getItems().add(dir);
        });
        colSettingsDirectoriesParent.setEditable(true);

        TableColumn<Directory, String> colSettingsDirectoriesPath = new TableColumn<>(this.lang.getString("settings.directories.path"));
        colSettingsDirectoriesPath.setCellValueFactory(param -> returnStringValue(param.getValue().getPath()));
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

        TableColumn<Directory, String> colSettingsDirectoriesIcon = new TableColumn<>(this.lang.getString("settings.directories.icon"));
        colSettingsDirectoriesIcon.setCellValueFactory(param -> returnStringValue(param.getValue().getFolder().getIcon()));
        colSettingsDirectoriesIcon.setEditable(true);
        colSettingsDirectoriesIcon.setOnEditStart(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(this.lang.getString("settings.directories.icon"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Icon", "*.ico"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
            File file = fileChooser.showOpenDialog(null);
            if(file!=null) {
                event.getRowValue().getFolder().setIcon(file.getAbsolutePath());
                this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());
            }
        });

        List<BatchTemplate> batchTemplates = new LinkedList<>();
        try {
            batchTemplates.addAll(PhotoManager.GLOBALS.getDatabase().getBatchTemplates(""));
        } catch (Exception ignored) {}
        TableColumn<Directory, BatchTemplate> colSettingsDirectoriesBatch = new TableColumn<>(this.lang.getString("main.menu.program.batch"));
        colSettingsDirectoriesBatch.setCellValueFactory(param -> returnBatchTemplateValue(param.getValue().getFolder().getBatchTemplate()));
        colSettingsDirectoriesBatch.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(BatchTemplate object) {
                if (object != null) {
                    return object.getTitle();
                } else {
                    return "";
                }
            }

            @Override
            public BatchTemplate fromString(String string) {
                if (string != null) {
                    for (BatchTemplate batchTemplate : batchTemplates) {
                        if (batchTemplate != null) {
                            if (batchTemplate.getTitle().equals(string)) {
                                return batchTemplate;
                            }
                        }
                    }
                }
                return null;
            }
        }, FXCollections.observableList(batchTemplates)));
        colSettingsDirectoriesBatch.setOnEditCommit(event -> {
            BatchTemplate batchTemplate = event.getNewValue();
            event.getRowValue().getFolder().setBatchTemplate(batchTemplate);
        });
        colSettingsDirectoriesBatch.setEditable(true);

        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesTitle);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesPath);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesParent);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesIcon);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesBatch);
        Directory directory = new Directory();
        directory.setFolder(new Folder());
        this.tblSettingsDirectories.getItems().add(directory);
        this.fixColumns(Arrays.asList(1, 2, 2, 2, 1));
    }

    private ObservableValue<String> returnStringValue(String item) {
        return new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super String> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super String> listener) {

            }

            @Override
            public String getValue() {
                return item;
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        };
    }

    private ObservableValue<BatchTemplate> returnBatchTemplateValue(BatchTemplate item) {
        return new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super BatchTemplate> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super BatchTemplate> listener) {

            }

            @Override
            public BatchTemplate getValue() {
                return item;
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        };
    }

    private ObservableValue<Directory> returnDirectoryValue(Directory item) {
        return new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super Directory> listener) {

            }

            @Override
            public void removeListener(ChangeListener<? super Directory> listener) {

            }

            @Override
            public Directory getValue() {
                return item;
            }

            @Override
            public void addListener(InvalidationListener listener) {

            }

            @Override
            public void removeListener(InvalidationListener listener) {

            }
        };
    }

    public void fixColumns(List<Integer> sizes) {
        int max = sizes.stream().mapToInt(cur -> cur).sum();
        double width = this.tblSettingsDirectories.getWidth() - 20;
        double factor = width / max;

        for(int i = 0; i<=this.tblSettingsDirectories.getColumns().size() - 1; i++) {
            this.tblSettingsDirectories.getColumns().get(i).setPrefWidth(sizes.get(i) * factor);
            this.tblSettingsDirectories.getColumns().get(i).setMinWidth(sizes.get(i) * factor);
        }
    }

    private void initValidator() {
        this.validator.validateInteger(this.txtSettingsZoomFactor, true, this.accSettings.getPanes().get(0));
        this.validator.validateFieldsTogetherMandatory(Arrays.asList(this.txtSettingsUnsplashKey, this.txtSettingsUnsplashSecretKey), this.accSettings.getPanes().get(3));
        this.validator.validateFieldsTogetherMandatory(Arrays.asList(this.txtSettingsInstagramUser, this.txtSettingsInstagramPwd), this.accSettings.getPanes().get(5));
    }

    @Override
    public void init(MainController mainController) {
        this.mainController = mainController;
        this.fillData();
        this.initDirTableView();
        this.initValidator();
    }
}
