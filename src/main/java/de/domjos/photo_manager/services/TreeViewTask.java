package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.SettingsController;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Folder;
import de.domjos.photo_manager.settings.Globals;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.util.List;

public final class TreeViewTask extends ParentTask<TreeItem<Directory>> {

    public TreeViewTask(ProgressBar progressBar, Label messages) {
        super(progressBar, messages);
    }

    @Override
    TreeItem<Directory> runBody() throws Exception {
        Platform.runLater(()-> PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.WAIT));
        Directory directory = PhotoManager.GLOBALS.getDatabase().getRoot();
        TreeItem<Directory> root = new TreeItem<>(directory, getIcon());
        root.setExpanded(true);
        addChildren(directory, root);
        addFolder(root);
        return root;
    }

    private void addChildren(Directory directory, TreeItem<Directory> parent) {
        for(Directory child : directory.getChildren()) {
            TreeItem<Directory> childItem = new TreeItem<>(child, this.getIcon());
            childItem.setExpanded(true);
            parent.getChildren().add(childItem);
            this.addChildren(child, childItem);
        }
    }

    private void addFolder(TreeItem<Directory> parent) {
        List<SettingsController.DirRow> dirRowList = SettingsController.getRowsFromSettings();
        for(SettingsController.DirRow dirRow : dirRowList) {
            Folder folder = new Folder();
            folder.setTitle(dirRow.getTitle());
            folder.setPath(dirRow.getPath());
            folder.setIcon(dirRow.getIcon());

            TreeItem<Directory> childItem = new TreeItem<>(folder, this.getIcon(folder.getIcon()));
            parent.getChildren().add(childItem);
        }

        String deleteFolder = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_DELETE_KEY, "");
        if(!deleteFolder.trim().isEmpty()) {
            Folder folder = new Folder();
            folder.setTitle(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.bin"));
            folder.setPath(deleteFolder);
            folder.setIcon("/images/icons/delete.png");

            TreeItem<Directory> childItem = new TreeItem<>(folder, this.getIconFromResource(folder.getIcon()));
            parent.getChildren().add(childItem);
        }
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
            return new ImageView(
                new javafx.scene.image.Image(new FileInputStream(path), 16, 16, true, true)
            );
        } catch (Exception ex) {
            return this.getIconFromResource("/images/icons/system_folder.png");
        }
    }
}
