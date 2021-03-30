package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public final class SaveFolderTask extends ParentTask<Void> {
    private final String message;
    private final Directory directory;
    private final long parent;
    private final boolean recursive;
    public IntegerProperty counter = new SimpleIntegerProperty(0);
    public int max = 0;

    public SaveFolderTask(ProgressBar progressBar, Label messages, String message, Directory directory, long parent, boolean recursive) {
        super(progressBar, messages);

        this.message = message;
        this.directory = directory;
        this.parent = parent;
        this.recursive = recursive;

        this.counter.addListener((observableValue, number, t1) -> updateProgress(t1.intValue(), this.max));
    }

    @Override
    Void runBody() throws Exception {
        updateMessage(this.message);
        updateProgress(0, 4);

        List<Directory> directories = PhotoManager.GLOBALS.getDatabase().getDirectories("id=" + this.parent, true);
        if(!directories.isEmpty()) {
            Directory parent = directories.get(0);
            updateProgress(1, 4);
            Directory tree = SaveFolderTask.generateTree(this.directory.getPath(), this.recursive);
            if(tree != null) {
                tree.setTitle(this.directory.getTitle());
                tree.setLibrary(true);
                updateProgress(2, 4);
                parent.getChildren().add(tree);
                save(tree, parent.getId());
                updateProgress(3, 4);
            }
        }


        updateProgress(4, 4);
        updateMessage("");
        return null;
    }

    public static Directory generateTree(String path, boolean recursive) throws Exception {
        File root = new File(path);
        if(root.exists() && root.isDirectory()) {
            Directory directory = new Directory();
            directory.setId(getDir(path));
            directory.setPath(path);
            directory.setTitle(root.getName());

            File[] files = root.listFiles(ImageHelper.getFilter());
            if(files != null) {
                for(File child : files) {
                    Image image = SaveFolderTask.fileToImage(getImage(child.getAbsolutePath()), child, directory);
                    directory.getImages().add(image);
                }
            }

            if(recursive) {
                File[] folders = root.listFiles();
                if(folders != null) {
                    for(File folder : folders) {
                        if(folder.isDirectory()) {
                            directory.getChildren().add(generateTree(folder.getAbsolutePath(), true));
                        }
                    }
                }
            }

            return directory;
        }
        return null;
    }

    public static Directory save(Directory current, long parent) throws Exception {
        current.setId(PhotoManager.GLOBALS.getDatabase().insertOrUpdateDirectory(current, parent, false));

        for(int i = 0; i<=current.getImages().size() - 1; i++) {
            Image image = current.getImages().get(i);
            image.setDirectory(current);
            PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
        }

        for(int i = 0; i<=current.getChildren().size() - 1; i++) {
            current.getChildren().set(i, save(current.getChildren().get(i), current.getId()));
        }
        return current;
    }

    private static long getDir(String path) {
        long id = 0;
        try {
            List<Directory> directories = PhotoManager.GLOBALS.getDatabase().getDirectories("path='" + path + "'", false);
            if(directories != null) {
                if(!directories.isEmpty()) {
                    id = directories.get(0).getId();
                }
            }
        } catch (Exception ignored) {}
        return id;
    }

    private static long getImage(String path) {
        long id = 0;
        try {
            List<Image> images = PhotoManager.GLOBALS.getDatabase().getImages("path='" + path + "'");
            if(images != null) {
                if(!images.isEmpty()) {
                    id = images.get(0).getId();
                }
            }
        } catch (Exception ignored) {}
        return id;
    }

    public static Image fileToImage(long id, File child, Directory parent) throws Exception {
        Image image = new Image();
        image.setId(id);
        image.setTitle(child.getName());
        image.setPath(child.getAbsolutePath());
        image.setDirectory(parent);
        BufferedImage bufferedImage = ImageHelper.getImage(child.getAbsolutePath());
        if(bufferedImage != null) {
            image.setWidth(bufferedImage.getWidth());
            image.setHeight(bufferedImage.getHeight());
            image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(bufferedImage, 80, 80)));
        }
        return image;
    }
}
