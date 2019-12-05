package de.domjos.photo_manager.services;

import com.tinify.Options;
import com.tinify.Source;
import com.tinify.Tinify;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public final class TinifyTask extends ParentTask<Void> {
    private int width, height;
    private List<Image> images;

    public TinifyTask(ProgressBar progressBar, Label messages, String width, String height, Object img) {
        super(progressBar, messages);
        Tinify.setKey(PhotoManager.GLOBALS.getDecryptedSetting(Globals.TINY_KEY, ""));

        this.width = -1;
        if(width!=null) {
            if(!width.trim().isEmpty()) {
                this.width = Integer.parseInt(width.trim());
            }
        }

        this.height = -1;
        if(height!=null) {
            if(!height.trim().isEmpty()) {
                this.height = Integer.parseInt(height.trim());
            }
        }

        this.images = new LinkedList<>();
        if(img instanceof Image) {
            this.images.add((Image) img);
        } else if(img instanceof Directory) {
            loadImagesFromDirectory((Directory) img);
        } else if(img instanceof List) {
            for(Object obj : (List) img) {
                this.images.add((Image) obj);
            }
        }
    }

    @Override
    protected Void runBody() {
        int i = 0;
        updateProgress(i, this.images.size());
        for(Image image : this.images) {
            try {
                updateMessage(PhotoManager.GLOBALS.getLanguage().getString("main.image.services.tinify.compress"));
                Source source = Tinify.fromBuffer(ImageHelper.imageToByteArray(ImageHelper.getImage(image.getPath())));
                source = this.scaleImage(source);

                String file = this.getNewFileName(image);
                source.toFile(file);

                this.saveImage(file, image);

                i++;
                updateProgress(i, this.images.size());
            } catch (Exception ex) {
                Platform.runLater(()->Dialogs.printException(ex));
            }
        }
        updateMessage("");
        return null;
    }

    private void loadImagesFromDirectory(Directory directory) {
        try {
            if(!directory.getChildren().isEmpty()) {
                for(Directory child : directory.getChildren()) {
                    this.loadImagesFromDirectory(child);
                }
            }

            this.images.addAll(PhotoManager.GLOBALS.getDatabase().getImages(directory, false));
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private String getNewFileName(Image image) {
        String path = image.getPath();
        File originalFile = new File(path);
        String fileWithoutName = originalFile.getAbsolutePath().replace(originalFile.getName(), "");
        String[] name = originalFile.getName().split("\\.");
        return fileWithoutName + PhotoManager.GLOBALS.getDecryptedSetting(Globals.TINY_FILE, "").replace("[old]", name[0]) + "." + name[1];
    }

    private void saveImage(String file, Image image) throws Exception {
        updateMessage(PhotoManager.GLOBALS.getLanguage().getString("main.image.services.tinify.save"));
        Image tinified = new Image();
        tinified.setThumbnail(image.getThumbnail());
        tinified.setPath(file);
        tinified.setTitle(new File(file).getName());
        tinified.setDirectory(image.getDirectory());
        PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(tinified);
    }

    private Source scaleImage(Source source) {
        updateMessage(PhotoManager.GLOBALS.getLanguage().getString("main.image.services.tinify.scale"));
        Options options = new Options();
        if(this.width!=-1 && this.height!=-1) {
            options.with("method", "fit");
            options.with("width", this.width);
            options.with("height", this.height);
            return source.resize(options);
        } else if(this.width!=-1 || this.height!=-1) {
            options.with("method", "scale");
            if(this.width!=-1) {
                options.with("width", this.width);
            } else {
                options.with("height", this.height);
            }
            return source.resize(options);
        }
        return source;
    }
}
