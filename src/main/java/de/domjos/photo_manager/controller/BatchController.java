package de.domjos.photo_manager.controller;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.subController.ParentController;
import de.domjos.photo_manager.model.gallery.BatchTemplate;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.services.BatchTask;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class BatchController extends ParentController {
    private List<Image> images = new LinkedList<>();

    private @FXML Button cmdBatchHome, cmdBatchStart, cmdBatchTemplateSave, cmdBatchTemplateDelete;
    private @FXML ComboBox<BatchTemplate> cmbBatchTemplateName;

    private @FXML TextField txtBatchScaleWidth, txtBatchScaleHeight;
    private @FXML CheckBox chkBatchScaleCompress;

    private @FXML TextField txtBatchRename;

    private @FXML CheckBox chkBatchTargetFolder, chkBatchTargetFtp, chkBatchTargetFtpSecure;
    private @FXML TreeView<Directory> tvBatchTargetFolder, tvBatchTargetFtpFolder;
    private @FXML TextField txtBatchTargetFtpUser, txtBatchTargetFtpServer, txtBatchTargetFtpPwd;

    private @FXML ListView<Image> lvBatchSelectedImages;

    @Override
    public void initialize(ResourceBundle resources) {
        this.tvBatchTargetFtpFolder.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                BatchTemplate batchTemplate = new BatchTemplate();
                this.fieldsToBatchTemplate(batchTemplate);

                BatchTask.addFoldersToTreeView(batchTemplate, newValue);
                newValue.setExpanded(true);
            } catch (Exception ignored) {}
        }));

        this.cmdBatchStart.setOnAction(event -> {
            BatchTemplate batchTemplate = new BatchTemplate();
            this.fieldsToBatchTemplate(batchTemplate);

            Label lbl = this.mainController.getMessages();
            ProgressBar pb = this.mainController.getProgressBar();

            BatchTask batchTask = new BatchTask(pb, lbl, batchTemplate, this.images);
            batchTask.onFinish(()-> {
                String title = PhotoManager.GLOBALS.getLanguage().getString("batch.msg.finish");
                String content = PhotoManager.GLOBALS.getLanguage().getString("batch.msg.finish.content");

                Dialogs.printNotification(Alert.AlertType.CONFIRMATION, title, content);
            });
            new Thread(batchTask).start();
        });

        this.cmbBatchTemplateName.setConverter(new StringConverter<>() {
            @Override
            public String toString(BatchTemplate object) {
                if(object != null) {
                    return object.getTitle();
                } else {
                    return "";
                }
            }

            @Override
            public BatchTemplate fromString(String string) {
                for(BatchTemplate batchTemplate : cmbBatchTemplateName.getItems()) {
                    if(batchTemplate.getTitle().equals(string)) {
                        return batchTemplate;
                    }
                }
                return null;
            }
        });

        this.cmdBatchHome.setOnAction(event -> this.mainController.back());

        this.cmbBatchTemplateName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.batchTemplateToFields(newValue));

        this.cmdBatchTemplateSave.setOnAction(event -> {
            try {
                BatchTemplate batchTemplate = new BatchTemplate();
                if(this.cmbBatchTemplateName.getSelectionModel().isEmpty()) {
                    batchTemplate.setTitle(this.cmbBatchTemplateName.getEditor().getText());
                } else {
                    batchTemplate = this.cmbBatchTemplateName.getSelectionModel().getSelectedItem();
                }
                this.fieldsToBatchTemplate(batchTemplate);

                PhotoManager.GLOBALS.getDatabase().insertOrUpdateBatchTemplate(batchTemplate);
                this.reloadBatchTemplates();
                this.cmbBatchTemplateName.getEditor().setText("");
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.cmdBatchTemplateDelete.setOnAction(event -> {
            try {
                if(!this.cmbBatchTemplateName.getSelectionModel().isEmpty()) {
                    PhotoManager.GLOBALS.getDatabase().deleteBatchTemplate(this.cmbBatchTemplateName.getSelectionModel().getSelectedItem());
                }
                this.reloadBatchTemplates();
                this.cmbBatchTemplateName.getEditor().setText("");
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.txtBatchTargetFtpServer.textProperty().addListener(observable -> this.initFTPDirectories());
        this.txtBatchTargetFtpUser.textProperty().addListener(observable -> this.initFTPDirectories());
        this.txtBatchTargetFtpPwd.textProperty().addListener(observable -> this.initFTPDirectories());
    }

    public void reloadBatchTemplates() {
        try {
            this.cmbBatchTemplateName.getItems().clear();
            this.cmbBatchTemplateName.getItems().addAll(PhotoManager.GLOBALS.getDatabase().getBatchTemplates(""));
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    public void addImages(List<Image> images) {
        this.images = images;
        this.lvBatchSelectedImages.getItems().clear();
        this.lvBatchSelectedImages.getItems().addAll(this.images);
    }


    public ListView<Image> getBatchSelectedImages() {
        return this.lvBatchSelectedImages;
    }

    public TreeView<Directory> getBatchTargetFolder() {
        return this.tvBatchTargetFolder;
    }

    private void fieldsToBatchTemplate(BatchTemplate batchTemplate) {
        try {
            batchTemplate.setWidth(Integer.parseInt(this.txtBatchScaleWidth.getText()));
        } catch (Exception ex) {
            batchTemplate.setWidth(-1);
        }
        try {
            batchTemplate.setHeight(Integer.parseInt(this.txtBatchScaleHeight.getText()));
        } catch (Exception ex) {
            batchTemplate.setHeight(-1);
        }
        batchTemplate.setCompress(this.chkBatchScaleCompress.isSelected());
        batchTemplate.setRename(this.txtBatchRename.getText());
        batchTemplate.setFolder(this.chkBatchTargetFolder.isSelected());
        if(!this.tvBatchTargetFolder.getSelectionModel().isEmpty()) {
            batchTemplate.setTargetFolder(this.tvBatchTargetFolder.getSelectionModel().getSelectedItem().getValue());
        } else {
            batchTemplate.setTargetFolder(null);
        }
        batchTemplate.setFtp(this.chkBatchTargetFtp.isSelected());
        batchTemplate.setServer(this.txtBatchTargetFtpServer.getText());
        batchTemplate.setUser(this.txtBatchTargetFtpUser.getText());
        batchTemplate.setPassword(this.txtBatchTargetFtpPwd.getText());
        batchTemplate.setFtpSecure(this.chkBatchTargetFtpSecure.isSelected());
        if(!this.tvBatchTargetFtpFolder.getSelectionModel().isEmpty()) {
            batchTemplate.setTargetFolderFtp(this.tvBatchTargetFtpFolder.getSelectionModel().getSelectedItem().getValue());
        } else {
            batchTemplate.setTargetFolderFtp(null);
        }
    }

    private void batchTemplateToFields(BatchTemplate batchTemplate) {
        if(batchTemplate == null) {
            batchTemplate = new BatchTemplate();
        }

        if(batchTemplate.getWidth() != -1) {
            this.txtBatchScaleWidth.setText(String.valueOf(batchTemplate.getWidth()));
        } else {
            this.txtBatchScaleWidth.setText("");
        }
        if(batchTemplate.getHeight() != -1) {
            this.txtBatchScaleHeight.setText(String.valueOf(batchTemplate.getHeight()));
        } else {
            this.txtBatchScaleHeight.setText("");
        }
        this.chkBatchScaleCompress.setSelected(batchTemplate.isCompress());
        this.txtBatchRename.setText(batchTemplate.getRename());
        this.chkBatchTargetFolder.setSelected(batchTemplate.isFolder());
        if(batchTemplate.getTargetFolder() != null) {
            this.selectDirectory(this.tvBatchTargetFolder.getRoot(), batchTemplate.getTargetFolder());
        } else {
            this.tvBatchTargetFolder.getSelectionModel().clearSelection();
        }
        this.chkBatchTargetFtp.setSelected(batchTemplate.isFtp());
        this.txtBatchTargetFtpServer.setText(batchTemplate.getServer());
        this.txtBatchTargetFtpUser.setText(batchTemplate.getUser());
        this.txtBatchTargetFtpPwd.setText(batchTemplate.getPassword());
        this.chkBatchTargetFtpSecure.setSelected(batchTemplate.isFtpSecure());
        if(batchTemplate.getTargetFolderFtp() != null) {
            this.selectFtpDirectory(this.tvBatchTargetFtpFolder.getRoot(), batchTemplate.getTargetFolderFtp().getPath());
        } else {
            this.tvBatchTargetFtpFolder.getSelectionModel().clearSelection();
        }
    }

    private void selectFtpDirectory(TreeItem<Directory> current, String path) {
        this.tvBatchTargetFtpFolder.getSelectionModel().select(current);
        if(current.getValue().getPath().equals(path)) {
            return;
        }
        for(TreeItem<Directory> sub : current.getChildren()) {
            if(path.startsWith(sub.getValue().getPath()) && !sub.getValue().getTitle().equals(".") && !sub.getValue().getTitle().equals("..")) {
                this.selectFtpDirectory(sub, path);
            }
        }
    }

    private void selectDirectory(TreeItem<Directory> current, Directory directory) {
        if(directory.getId() != 0) {
            if(current.getValue().getId() == directory.getId()) {
                this.tvBatchTargetFolder.getSelectionModel().select(current);
            }
        } else {
            if(current.getValue().getTitle().trim().equals(directory.getTitle())) {
                this.tvBatchTargetFolder.getSelectionModel().select(current);
            }
        }
        for(TreeItem<Directory> dir : current.getChildren()) {
            this.selectDirectory(dir, directory);
        }
    }

    private void initFTPDirectories() {
        try {
            this.tvBatchTargetFtpFolder.setRoot(null);
            Directory directory = new Directory();
            directory.setPath("/");
            directory.setTitle("Root");
            this.tvBatchTargetFtpFolder.setRoot(new TreeItem<>(directory, BatchTask.getFolderIcon()));
        } catch (Exception ignored) {}
    }
}
