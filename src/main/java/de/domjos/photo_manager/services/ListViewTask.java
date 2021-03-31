package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public final class ListViewTask extends ParentTask<List<Image>> {
    private final Directory directory;
    private final String search;

    public ListViewTask(ProgressBar progressBar, Label messages, Directory directory, String search) {
        super(progressBar, messages);

        this.directory = directory;
        this.search = search;
    }

    @Override
    List<Image> runBody() {
        List<Image> list = new LinkedList<>();
        try {
            if (directory != null) {

                if (directory.getFolder() != null) {
                    this.updateDBByFileSystem();
                }

                List<Image> images = PhotoManager.GLOBALS.getDatabase().getImages("parent=" + this.directory.getId());
                int max = images.size();
                int index = 0;
                updateProgress(index, max);

                for (Image image : images) {
                    if (!new File(image.getPath()).exists()) {
                        PhotoManager.GLOBALS.getDatabase().deleteImage(image);
                        continue;
                    }

                    boolean foundItem = true;
                    if (!this.search.isEmpty()) {
                        foundItem = false;
                        if (image.getCategory() != null) {
                            if (image.getCategory().getTitle().trim().toLowerCase().contains(this.search)) {
                                foundItem = true;
                            }
                        }
                        if (!image.getTags().isEmpty()) {
                            for (DescriptionObject descriptionObject : image.getTags()) {
                                if (descriptionObject.getTitle().trim().toLowerCase().contains(this.search)) {
                                    foundItem = true;
                                }
                            }
                        }
                    }
                    Dimension dimension = ImageHelper.getSize(image.getPath());
                    image.setWidth(dimension.width);
                    image.setHeight(dimension.height);
                    if (foundItem) {
                        list.add(image);
                    }
                }
            }
        } catch (Exception ex) {
            Platform.runLater(()->Dialogs.printException(ex));
        }
        return list;
    }

    private void updateDBByFileSystem() throws Exception {
        File currentFolder = new File(this.directory.getPath());
        File[] content = currentFolder.listFiles(ImageHelper.getFilter());
        List<Image> images = PhotoManager.GLOBALS.getDatabase().getImages("parent=" + this.directory.getId());

        List<Image> imagesToDelete = new LinkedList<>();
        for(Image image : images) {
            boolean exists = false;
            if(content != null) {
                for(File child : content) {
                    if(image.getPath().trim().equals(child.getAbsolutePath().trim())) {
                        exists = true;
                        break;
                    }
                }
            }
            if(!exists) {
                imagesToDelete.add(image);
            }
        }

        for(Image image : imagesToDelete) {
            PhotoManager.GLOBALS.getDatabase().deleteImage(image);
        }

        List<Image> imagesToAdd = new LinkedList<>();
        if(content != null) {
            for(File child : content) {
                boolean exists = false;
                for(Image image : images) {
                    if(image.getPath().trim().equals(child.getAbsolutePath().trim())) {
                        exists = true;
                        break;
                    }
                }

                if(!exists) {
                    imagesToAdd.add(SaveFolderTask.fileToImage(0, child, this.directory, true));
                }
            }
        }

        List<Image> imagesToUpdate = new LinkedList<>();
        for(Image image : images) {
            boolean exists = false;
            for(Image newImage : imagesToAdd) {
                if(image.getPath().trim().equals(newImage.getPath().trim())) {
                    exists = true;
                    break;
                }
            }

            if(!exists) {
                imagesToUpdate.add(image);
            }
        }

        for(Image image : imagesToAdd) {
            PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
        }
        for(Image image : imagesToUpdate) {
            PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
        }
    }
}
