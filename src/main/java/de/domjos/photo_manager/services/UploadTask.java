package de.domjos.photo_manager.services;

import com.github.sardine.DavResource;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.Collections;
import java.util.List;

public final class UploadTask extends ParentTask<Void> {
    private List<Image> images;
    private WebDav webDav;
    private DavResource resource;

    public UploadTask(ProgressBar progressBar, Label messages, Image image, WebDav webDav, DavResource resource) {
        this(progressBar, messages, Collections.singletonList(image), webDav, resource);
    }

    public UploadTask(ProgressBar progressBar, Label messages, Directory directory, WebDav webDav, DavResource resource) throws Exception {
        super(progressBar, messages);
        this.images = PhotoManager.GLOBALS.getDatabase().getImages(directory, false);
        this.webDav = webDav;
        this.resource = resource;
    }

    private UploadTask(ProgressBar progressBar, Label messages, List<Image> images, WebDav webDav, DavResource resource) {
        super(progressBar, messages);
        this.images = images;
        this.webDav = webDav;
        this.resource = resource;
    }

    @Override
    Void runBody() throws Exception {
        super.updateProgress(0, this.images.size());

        for(int i = 1; i<=this.images.size(); i++) {
            this.webDav.put(this.resource, this.images.get(i - 1));
            super.updateProgress(i, this.images.size());
        }
        return null;
    }
}
