package de.domjos.photo_manager.controller;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.subController.ParentController;
import de.domjos.photo_manager.helper.InitializationHelper;
import de.domjos.photo_manager.model.gallery.BatchTemplate;
import de.domjos.photo_manager.services.WebDav;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.CryptoUtils;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private @FXML Button cmdSettingsSave, cmdSettingsHome;
    private @FXML CheckBox chkSettingsDebugMode;

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
    private @FXML TableView<DirRow> tblSettingsDirectories;

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
            PhotoManager.GLOBALS.saveSetting(Globals.DEBUG, this.chkSettingsDebugMode.isSelected(), false);
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
        });

        this.cmdSettingsCloudTest.setOnAction(event -> {
            WebDav webDav = new WebDav(this.txtSettingsCloudUserName.getText(), this.txtSettingsCloudPassword.getText(), this.txtSettingsCloudPath.getText());

            Color color = webDav.testConnection() ? Color.GREEN : Color.RED;
            Background background = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
            this.cmdSettingsCloudTest.setBackground(background);
        });
    }

    private void updateProgram() {
        PhotoManager.GLOBALS.setDebugMode(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false));
        PhotoManager.GLOBALS.getStage().setTitle(InitializationHelper.getHeader());
        mainController.initTinify();
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
        this.txtSettingsInstagramUser.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_USER, ""));
        this.txtSettingsInstagramPwd.setText(PhotoManager.GLOBALS.getDecryptedSetting(Globals.INSTAGRAM_PWD, ""));
        SettingsController.getRowsFromSettings().forEach(this.tblSettingsDirectories.getItems()::add);
        this.tblSettingsDirectories.getItems().add(new DirRow());

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

        String setting = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES, "");
        for(String row : setting.split(";")) {
            if(row.contains(",")) {
                String[] item = row.trim().split(",");
                if(item.length >= 2) {
                    DirRow dirRow = new DirRow();
                    dirRow.setTitle(item[0]);
                    dirRow.setPath(item[1]);
                    if(item.length > 2) {
                        dirRow.setIcon(item[2]);
                    }
                    if(item.length > 3) {
                        try {
                            int id = Integer.parseInt(item[3].trim());
                            dirRow.batch = PhotoManager.GLOBALS.getDatabase().getBatchTemplates("id=" + id).get(0);
                        } catch (Exception ignored) {}
                    }
                    if(item.length > 4) {
                        dirRow.setEncryption(item[4].trim());
                    } else {
                        dirRow.setEncryption("");
                    }
                    dirRows.add(dirRow);
                }
            }
        }

        return dirRows;
    }

    private void saveRowsToSettings() {
        boolean isEmpty = true;
        for(DirRow dirRow : this.tblSettingsDirectories.getItems()) {
            if(!dirRow.getTitle().trim().isEmpty() && !dirRow.getPath().trim().isEmpty()) {
                PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES, this.generateSetting(), false);
                isEmpty = false;
            }
        }

        if(isEmpty) {
            PhotoManager.GLOBALS.saveSetting(Globals.DIRECTORIES, "", false);
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

        TableColumn<DirRow, String> colSettingsDirectoriesTitle = new TableColumn<>(this.lang.getString("settings.directories.title"));
        colSettingsDirectoriesTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSettingsDirectoriesTitle.setCellFactory(TextFieldTableCell.forTableColumn());
        colSettingsDirectoriesTitle.setOnEditCommit(event -> {
            String val = event.getNewValue();
            event.getRowValue().setTitle(val);
            this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());
            this.tblSettingsDirectories.getItems().add(new DirRow());
        });
        colSettingsDirectoriesTitle.setEditable(true);

        TableColumn<DirRow, String> colSettingsDirectoriesPath = new TableColumn<>(this.lang.getString("settings.directories.path"));
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

        TableColumn<DirRow, String> colSettingsDirectoriesIcon = new TableColumn<>(this.lang.getString("settings.directories.icon"));
        colSettingsDirectoriesIcon.setCellValueFactory(new PropertyValueFactory<>("icon"));
        colSettingsDirectoriesIcon.setEditable(true);
        colSettingsDirectoriesIcon.setOnEditStart(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(this.lang.getString("settings.directories.icon"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Icon", "*.ico"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
            File file = fileChooser.showOpenDialog(null);
            if(file!=null) {
                event.getRowValue().setIcon(file.getAbsolutePath());
                this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());
            }
        });

        List<BatchTemplate> batchTemplates = new LinkedList<>();
        try {
            batchTemplates.addAll(PhotoManager.GLOBALS.getDatabase().getBatchTemplates(""));
        } catch (Exception ignored) {}
        TableColumn<DirRow, BatchTemplate> colSettingsDirectoriesBatch = new TableColumn<>(this.lang.getString("main.menu.program.batch"));
        colSettingsDirectoriesBatch.setCellValueFactory(new PropertyValueFactory<>("batch"));
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
            event.getRowValue().setBatch(batchTemplate);
        });
        colSettingsDirectoriesBatch.setEditable(true);

        TableColumn<DirRow, String> colSettingsDirectoriesEncryption = new TableColumn<>(this.lang.getString("settings.directories.encryption"));
        colSettingsDirectoriesEncryption.setCellValueFactory(new PropertyValueFactory<>("encryption"));
        colSettingsDirectoriesEncryption.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(String s) {
                return CryptoUtils.encrypt(s);
            }

            @Override
            public String fromString(String s) {
                return CryptoUtils.decrypt(s);
            }
        }));
        colSettingsDirectoriesEncryption.setEditable(true);
        colSettingsDirectoriesEncryption.setOnEditStart(event -> {
            Dialog<String> dialog = Dialogs.createPasswordDialog(event.getRowValue().getEncryption());
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> {
                if(!s.trim().isEmpty()) {
                    event.getRowValue().setEncryption(s.trim());
                    this.tblSettingsDirectories.getItems().set(event.getTablePosition().getRow(), event.getRowValue());
                }
            });
            event.consume();
        });

        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesTitle);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesPath);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesIcon);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesBatch);
        this.tblSettingsDirectories.getColumns().add(colSettingsDirectoriesEncryption);
        this.tblSettingsDirectories.getItems().add(new DirRow());
        this.fixColumns(Arrays.asList(1, 2, 2, 1, 1));
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

    @Override
    public void init(MainController mainController) {
        this.mainController = mainController;
        this.fillData();
        this.initDirTableView();
    }

    private String generateSetting() {
        StringBuilder stringBuilder = new StringBuilder();
        for(DirRow dirRow : this.tblSettingsDirectories.getItems()) {
            if(dirRow.batch != null) {
                stringBuilder.append(String.format("%s,%s,%s,%d,%s;", dirRow.title, dirRow.path, dirRow.icon, dirRow.batch.getId(), dirRow.encryption));
            } else {
                stringBuilder.append(String.format("%s,%s,%s,%s,%s;", dirRow.title, dirRow.path, dirRow.icon, "", dirRow.encryption));
            }
        }
        return stringBuilder.toString();
    }

    public static class DirRow {
        private String title, path, icon, encryption;
        private BatchTemplate batch;

        public DirRow() {
            super();
            this.title = "";
            this.path = "";
            this.icon = "";
            this.batch = null;
            this.encryption = "";
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

        public BatchTemplate getBatch() {
            return this.batch;
        }

        public void setBatch(BatchTemplate batch) {
            this.batch = batch;
        }

        public String getEncryption() {
            return this.encryption;
        }

        public void setEncryption(String encryption) {
            this.encryption = encryption;
        }
    }
}
