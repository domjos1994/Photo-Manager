package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
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
import java.util.concurrent.atomic.AtomicBoolean;

public final class TreeViewTask extends ParentTask<TreeItem<Directory>> {

    public TreeViewTask(ProgressBar progressBar, Label messages) {
        super(progressBar, messages);
    }

    @Override
    TreeItem<Directory> runBody() throws Exception {
        Platform.runLater(()-> PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.WAIT));
        Directory directory = PhotoManager.GLOBALS.getDatabase().getDirectories("isRoot=1", true).get(0);
        String icon = "";
        if(directory.getFolder() != null) {
            icon = directory.getFolder().getIcon();
        }
        TreeItem<Directory> root = new TreeItem<>(directory, getIcon(icon, directory));
        root.setExpanded(true);
        this.addChildren(directory, root);
        return root;
    }

    private void addChildren(Directory directory, TreeItem<Directory> parent) {
        for(Directory child : directory.getChildren()) {
            AtomicBoolean pathContained = new AtomicBoolean(false);

            String icon = "";
            if(child.getFolder() != null) {
                icon = child.getFolder().getIcon();
            }
            TreeItem<Directory> childItem = new TreeItem<>(child, this.getIcon(icon, child));
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

    private Node getIcon(String path, Directory directory) {
        try {
            Node node = null;
            if(directory.isRoot()) {
                return this.getIconFromResource("/images/icons/root.png");
            }
            if(directory.isLibrary()) {
                return this.getIconFromResource("/images/icons/library.png");
            }

            if(directory.getFolder() != null) {
                if(!directory.getFolder().getPassword().trim().isEmpty()) {
                    node = this.getIconFromResource("/images/icons/system_folder.png");
                } else {
                    node = this.getIconFromResource("/images/icons/empty_folder.png");
                }
            } else {
                node = this.getIconFromResource("/images/icons/directory.png");
            }

            if(!path.isEmpty()) {
                File iconFile = new File(path);
                if(iconFile.exists()) {
                    if(iconFile.getAbsolutePath().trim().endsWith(".ico")) {
                        BufferedImage bufferedImage = Imaging.getBufferedImage(iconFile);
                        bufferedImage = ImageHelper.scale(bufferedImage, 16, 16);
                        node = new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
                    } else {
                        FileInputStream fileInputStream = new FileInputStream(iconFile);
                        ImageView imageView =  new ImageView(new Image(fileInputStream, 16, 16, true, true));
                        fileInputStream.close();
                        node = imageView;
                    }
                }
            }
            return node;
        } catch (Exception ex) {
            return this.getIconFromResource("/images/icons/empty_folder.png");
        }
    }

    private Node getIconFromResource(String res) {
        return new ImageView(
                new javafx.scene.image.Image(PhotoManager.class.getResourceAsStream(res), 16, 16, true, true)
        );
    }

}
