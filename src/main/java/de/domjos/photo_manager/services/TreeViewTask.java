package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.SettingsController;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Folder;
import de.domjos.photo_manager.settings.Globals;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.imaging.Imaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TreeViewTask extends ParentTask<TreeItem<Directory>> {
    private final List<Folder> folders;

    public TreeViewTask(ProgressBar progressBar, Label messages) {
        super(progressBar, messages);
        this.folders = new LinkedList<>();
    }

    @Override
    TreeItem<Directory> runBody() throws Exception {
        Platform.runLater(()-> PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.WAIT));
        Directory directory = PhotoManager.GLOBALS.getDatabase().getRoot();
        TreeItem<Directory> root = new TreeItem<>(directory, getIcon());
        root.setExpanded(true);
        this.getFoldersFromSettings();
        this.addChildren(directory, root);
        this.addFoldersToTreeView(root);
        return root;
    }

    private void addChildren(Directory directory, TreeItem<Directory> parent) {
        for(Directory child : directory.getChildren()) {
            AtomicBoolean pathContained = new AtomicBoolean(false);
            this.folders.forEach(folder -> {
                if(folder.getPath().equals(child.getPath())) {
                    pathContained.set(true);
                }
            });

            TreeItem<Directory> childItem = new TreeItem<>(child, this.getIcon());
            childItem.setExpanded(true);
            if(!pathContained.get()) {
                parent.getChildren().add(childItem);
            } else {
                if(!childItem.getChildren().isEmpty()) {
                    parent.getChildren().add(childItem);
                }
            }
            this.addChildren(child, childItem);
        }
    }

    private void getFoldersFromSettings() {
        List<SettingsController.DirRow> dirRowList = SettingsController.getRowsFromSettings();
        for(SettingsController.DirRow dirRow : dirRowList) {
            Folder folder = new Folder();
            folder.setTitle(dirRow.getTitle());
            folder.setPath(dirRow.getPath());
            folder.setIcon(dirRow.getIcon());
            folder.setDirRow(dirRow);
            if(new File(dirRow.getPath()).exists()) {
                this.folders.add(folder);
            }
        }

        String deleteFolder = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_DELETE_KEY, "");
        if(!deleteFolder.trim().isEmpty()) {
            Folder folder = new Folder();
            folder.setTitle(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.bin"));
            folder.setPath(deleteFolder);
            folder.setIcon("/images/icons/delete.png");
            if(new File(deleteFolder).exists()) {
                this.folders.add(folder);
            }
        }
    }

    private void addFoldersToTreeView(TreeItem<Directory> parent) {
        this.folders.forEach(folder -> {
            if(folder.getTitle().equals(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.bin"))) {
                TreeItem<Directory> childItem = new TreeItem<>(folder, this.getIconFromResource(folder.getIcon()));
                parent.getChildren().add(childItem);
            } else {
                TreeItem<Directory> childItem = new TreeItem<>(folder, this.getIcon(folder.getIcon()));
                parent.getChildren().add(childItem);
            }

        });
    }


    private Node getIcon() {
        return new ImageView(
                new javafx.scene.image.Image(PhotoManager.class.getResourceAsStream("/images/icons/directory.png"), 16, 16, true, true)
        );
    }

    private Node getIconFromResource(String res) {
        return new ImageView(
            new javafx.scene.image.Image(PhotoManager.class.getResourceAsStream(res), 16, 16, true, true)
        );
    }

    private Node getIcon(String path) {
        try {
            File iconFile = new File(path);
            if(iconFile.exists()) {
                if(iconFile.getAbsolutePath().trim().endsWith(".ico")) {
                    BufferedImage bufferedImage = Imaging.getBufferedImage(iconFile);
                    bufferedImage = ImageHelper.scale(bufferedImage, 16, 16);
                    return new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
                } else {
                    FileInputStream fileInputStream = new FileInputStream(iconFile);
                    ImageView imageView =  new ImageView(new Image(fileInputStream, 16, 16, true, true));
                    fileInputStream.close();
                    return imageView;
                }
            } else {
                return this.getIconFromResource("/images/icons/empty_folder.png");
            }
        } catch (Exception ex) {
            return this.getIconFromResource("/images/icons/empty_folder.png");
        }
    }
}
