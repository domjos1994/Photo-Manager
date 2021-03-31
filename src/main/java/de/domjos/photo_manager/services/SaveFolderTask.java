package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.utils.FileHelper;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public final class SaveFolderTask extends ParentTask<Void> {
    private final Directory directory;
    private final long parent;
    private final boolean recursive;

    public SaveFolderTask(ProgressBar progressBar, Label messages, Directory directory, long parent, boolean recursive) {
        super(progressBar, messages);

        this.directory = directory;
        this.parent = parent;
        this.recursive = recursive;
    }

    @Override
    Void runBody() throws Exception {
        long countFiles = FileHelper.countFiles(this.directory.getPath(), this.recursive) * 2;
        updateProgress(countFiles, PhotoManager.GLOBALS.getLanguage().getString("msg.saveFolder.count"));

        List<Directory> directories = PhotoManager.GLOBALS.getDatabase().getDirectories("id=" + this.parent, true);
        if(!directories.isEmpty()) {
            Directory parent = directories.get(0);
            Directory tree = FileHelper.generateTree(this.directory.getPath(), this.recursive, this, countFiles);

            if(tree != null) {
                tree.setTitle(this.directory.getTitle());
                tree.setLibrary(true);
                parent.getChildren().add(tree);

                save(tree, parent.getId(), this, countFiles);
            }
        }


        updateProgress(4, 4);
        updateMessage("");
        return null;
    }

    public static Directory save(Directory current, long parent, ParentTask<?> task, long max) throws Exception {
        current.setId(PhotoManager.GLOBALS.getDatabase().insertOrUpdateDirectory(current, parent, false));

        for(int i = 0; i<=current.getImages().size() - 1; i++) {
            Image image = current.getImages().get(i);
            image = SaveFolderTask.fileToImage(image.getId(), new File(image.getPath()), current, true);
            image.setDirectory(current);
            PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
            task.updateProgress(max, String.format(PhotoManager.GLOBALS.getLanguage().getString("msg.saveFolder.save"), image.getTitle()));
        }

        for(int i = 0; i<=current.getChildren().size() - 1; i++) {
            current.getChildren().set(i, save(current.getChildren().get(i), current.getId(), task, max));
        }
        return current;
    }

    public static Image fileToImage(long id, File child, Directory parent, boolean load) throws Exception {
        Image image = new Image();
        image.setId(id);
        image.setTitle(child.getName());
        image.setPath(child.getAbsolutePath());
        image.setDirectory(parent);
        if(load) {
            BufferedImage bufferedImage = ImageHelper.getImage(child.getAbsolutePath());
            if(bufferedImage != null) {
                image.setWidth(bufferedImage.getWidth());
                image.setHeight(bufferedImage.getHeight());
                image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(bufferedImage, 80, 80)));
            }
        }
        return image;
    }
}
