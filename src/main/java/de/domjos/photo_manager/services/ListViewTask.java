package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Folder;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class ListViewTask extends ParentTask<List<Image>> {
    private Directory directory;
    private String search;

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
                if (directory instanceof Folder) {
                    Folder folder = (Folder) directory;

                    long[] index = {0};
                    long max = Files.list(Paths.get(folder.getPath())).count();
                    updateProgress(index[0], max);
                    Files.list(Paths.get(folder.getPath())).forEach(path -> {
                        try {
                            File file = path.toFile();
                            if(file.isFile()) {
                                String extension = FilenameUtils.getExtension(file.getAbsolutePath());
                                if(Arrays.asList(ImageHelper.EXTENSIONS).contains(extension)) {
                                    Image image = new Image();
                                    image.setPath(file.getAbsolutePath());
                                    if(folder.getDirRow()!=null) {
                                        if(folder.getDirRow().getEncryption().trim().isEmpty()) {
                                            image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(ImageHelper.getImage(file.getAbsolutePath()), 50, 50)));
                                        } else {
                                            image.setThumbnail(ImageHelper.imageToByteArray(file.getAbsolutePath(), folder.getDirRow().getEncryption()));
                                        }
                                    } else {
                                        image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(ImageHelper.getImage(file.getAbsolutePath()), 50, 50)));
                                    }
                                    image.setTitle(file.getName());
                                    list.add(image);
                                }
                            }
                            index[0]++;
                            updateProgress(index[0], max);
                        } catch (Exception ex) {
                            Dialogs.printException(ex);
                        }
                    });
                } else {
                    List<Image> images = PhotoManager.GLOBALS.getDatabase().getImages(this.directory, false);
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

                        if (foundItem) {
                            list.add(image);
                        }
                    }
                    index++;
                    updateProgress(index, max);
                }
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
        return list;
    }
}
