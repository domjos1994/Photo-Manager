package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.gallery.MetaData;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.LinkedList;
import java.util.List;

public final class MapTask extends ParentTask<List<Image>> {
    private Directory directory;

    public MapTask(ProgressBar progressBar, Label messages, Directory directory) {
        super(progressBar, messages);
        this.directory = directory;
    }

    @Override
    List<Image> runBody() throws Exception {
        List<Image> allImages = new LinkedList<>();
        List<Image> images = this.getAllImages(this.directory, new LinkedList<>());
        this.updateProgress(0, images.size());

        for(int i = 0; i<=images.size()-1; i++) {
            MetaData metaData = ImageHelper.readMetaData(images.get(i).getPath());

            if(metaData.getLongitude()!=0 && metaData.getLongitude()!=0) {
               allImages.add(images.get(i));
            }

            this.updateProgress(i+1, images.size());
        }

        return allImages;
    }

    private List<Image> getAllImages(Directory directory, List<Image> images) throws Exception {
        if(directory==null) {
            directory = PhotoManager.GLOBALS.getDatabase().getRoot();
        }

        for(Directory child : directory.getChildren()) {
            images = this.getAllImages(child, images);
        }

        images.addAll(PhotoManager.GLOBALS.getDatabase().getImages(directory, false));
        return images;
    }
}
