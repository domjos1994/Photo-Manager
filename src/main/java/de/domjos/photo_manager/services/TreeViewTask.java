package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Directory;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

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

    private Node getIcon() {
        return new ImageView(
                new javafx.scene.image.Image(PhotoManager.class.getResourceAsStream("/images/icons/directory.png"), 16, 16, true, true)
        );
    }
}
