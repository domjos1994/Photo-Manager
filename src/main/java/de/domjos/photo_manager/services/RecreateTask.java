package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.Image;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.List;

public class RecreateTask extends ParentTask<Void> {
    private List<Image> images;

    public RecreateTask(ProgressBar progressBar, Label messages, List<Image> images) {
        super(progressBar, messages);

        this.images = images;
    }

    @Override
    Void runBody() throws Exception {
        this.updateProgress(0, this.images.size());
        for(int i = 0; i<=this.images.size()-1; i++) {
            Image image = this.images.get(i);
            image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(ImageHelper.getImage(image.getPath()), 50, 50)));
            PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
            this.updateProgress(i+1, this.images.size());
        }
        return null;
    }
}
