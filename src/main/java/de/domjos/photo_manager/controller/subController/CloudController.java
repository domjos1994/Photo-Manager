package de.domjos.photo_manager.controller.subController;

import com.github.sardine.DavResource;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.services.Cloud;
import de.domjos.photo_manager.model.services.DavItem;
import de.domjos.photo_manager.services.UploadTask;
import de.domjos.photo_manager.services.WebDav;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ResourceBundle;

public class CloudController extends ParentController {
    private @FXML TextField txtPath;
    private @FXML TreeView<DavItem> tvDirectories;
    private @FXML Button cmdUpload;

    private WebDav webDav;

    public TreeView<DavItem> getTreeView() {
        return this.tvDirectories;
    }

    public WebDav getWebDav() {
        return webDav;
    }

    public void fillCloudWithDefault() {
        try {
            TreeItem<DavItem> treeItem = new TreeItem<>();

            String path = PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PATH, "");
            String user = PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_USER, "");
            String pwd = PhotoManager.GLOBALS.getDecryptedSetting(Globals.CLOUD_PWD, "");

            this.webDav = new WebDav(user, pwd, path);
            if(this.webDav.testConnection()) {
                this.txtPath.setText(path);
                treeItem.setValue(new DavItem(this.webDav.listDirectoriesRecursive().get(0)));
                this.addChildren(treeItem);
                this.tvDirectories.setRoot(treeItem);
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    public void getCloudPath() {
        String path = "";
        if(!this.mainController.getLvMain().getSelectionModel().isEmpty()) {
            if(this.mainController.getLvMain().getSelectionModel().getSelectedItem().getCloud()!=null) {
                path = this.mainController.getLvMain().getSelectionModel().getSelectedItem().getCloud().getPath();
            }
        } else {
            if(!this.mainController.getTvMain().getSelectionModel().isEmpty()) {
                if(this.mainController.getTvMain().getSelectionModel().getSelectedItem().getValue().getCloud()!=null) {
                    path = this.mainController.getTvMain().getSelectionModel().getSelectedItem().getValue().getCloud().getPath();
                }
            }
        }

        this.findPath(null, path);
    }

    @Override
    public void initialize(ResourceBundle resources) {

        this.tvDirectories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue!=null) {
                    if(newValue.getValue().get().isDirectory()) {
                        String path = this.webDav.getBaseUrl() + newValue.getValue().get().getPath();
                        this.txtPath.setText(path);
                        this.webDav.readDirectory(path);

                        if(newValue.getChildren().isEmpty()) {
                            if(!path.equals(PhotoManager.GLOBALS.getSetting(Globals.CLOUD_PATH, ""))) {
                                this.webDav.readDirectory(path);
                                this.addChildren(newValue);
                                newValue.setExpanded(true);
                                this.setCloudPath();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.cmdUpload.setOnAction(event -> {
            try {
                if(!this.mainController.getLvMain().getSelectionModel().isEmpty()) {
                    DavResource davResource = this.tvDirectories.getSelectionModel().getSelectedItem().getValue().get();
                    Image image = this.mainController.getLvMain().getSelectionModel().getSelectedItem();
                    UploadTask uploadTask = new UploadTask(this.mainController.getProgressBar(), this.mainController.getMessages(), image, this.webDav, davResource);
                    uploadTask.onFinish(()->
                            Dialogs.printNotification(
                                    Alert.AlertType.INFORMATION,
                                    resources.getString("main.image.services.cloud.finish"),
                                    resources.getString("main.image.services.cloud.finish")
                            )
                    );
                    new Thread(uploadTask).start();
                } else {
                    if(!this.mainController.getTvMain().getSelectionModel().isEmpty()) {
                        DavResource davResource = this.tvDirectories.getSelectionModel().getSelectedItem().getValue().get();
                        Directory directory = this.mainController.getTvMain().getSelectionModel().getSelectedItem().getValue();
                        UploadTask uploadTask = new UploadTask(this.mainController.getProgressBar(), this.mainController.getMessages(), directory, this.webDav, davResource);
                        uploadTask.onFinish(()->
                                Dialogs.printNotification(
                                        Alert.AlertType.INFORMATION,
                                        resources.getString("main.image.services.cloud.finish"),
                                        resources.getString("main.image.services.cloud.finish")
                                )
                        );
                        new Thread(uploadTask).start();
                    }
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
    }

    private void addChildren(TreeItem<DavItem> parent) {
        for(int i = 1; i<=this.webDav.listDirectoriesRecursive().size()-1; i++) {
            TreeItem<DavItem> child = new TreeItem<>();
            child.setValue(new DavItem(this.webDav.listDirectoriesRecursive().get(i)));
            parent.getChildren().add(child);
        }
    }

    private void setCloudPath() {
        Cloud cloud = new Cloud();
        cloud.setPath(this.txtPath.getText());
        if(!this.mainController.getLvMain().getSelectionModel().isEmpty()) {
            this.mainController.getLvMain().getSelectionModel().getSelectedItem().setCloud(cloud);
        } else {
            if(!this.mainController.getTvMain().getSelectionModel().isEmpty()) {
                this.mainController.getTvMain().getSelectionModel().getSelectedItem().getValue().setCloud(cloud);
                if(this.mainController.getDirectory()!=null) {
                    this.mainController.getDirectory().setCloud(cloud);
                }
            }
        }
    }

    private void findPath(TreeItem<DavItem> item, String path) {
        try {
            if(!path.trim().isEmpty()) {
                if(item==null) {
                    String completeUrl = this.webDav.getBaseUrl() + this.tvDirectories.getRoot().getValue().get().getPath();
                    while(!completeUrl.equals(path)) {
                        for(TreeItem<DavItem> davResourceTreeItem : this.tvDirectories.getRoot().getChildren()) {
                            if(path.contains(davResourceTreeItem.getValue().get().getPath())) {
                                this.tvDirectories.getSelectionModel().select(davResourceTreeItem);
                                this.findPath(davResourceTreeItem, path);
                                return;
                            }
                        }
                    }
                } else {
                    String completeUrl = this.webDav.getBaseUrl() + item.getValue().get().getPath();
                    while(!completeUrl.equals(path)) {
                        for(TreeItem<DavItem> davResourceTreeItem : item.getChildren()) {
                            if(path.contains(davResourceTreeItem.getValue().get().getPath())) {
                                this.tvDirectories.getSelectionModel().select(davResourceTreeItem);
                                this.findPath(davResourceTreeItem, path);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }
}
