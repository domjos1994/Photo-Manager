package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public final class ScaleImages extends ParentTask<Void> {
    private int width, height;
    private Directory directory;
    private List<Image> images;

    public ScaleImages(ProgressBar progressBar, Label messages, int width, int height, Directory directory, List<Image> images) {
        super(progressBar, messages);
        this.width = width;
        this.height = height;
        this.directory = directory;
        this.images = images;
    }

    @Override
    Void runBody() throws Exception {
        int max = this.images.size();
        this.updateProgress(0, max);

        for(int i = 0; i<=max-1; i++) {
            BufferedImage bufferedImage = ImageIO.read(new File(this.images.get(i).getPath()));

            int scaledWidth, scaledHeight;
            if(this.width == -1 && this.height != -1) {
                scaledHeight = this.height;
                scaledWidth = bufferedImage.getWidth() / (bufferedImage.getHeight() / scaledHeight);
            } else if(this.height == -1 && this.width != -1) {
                scaledWidth = this.width;
                scaledHeight = bufferedImage.getHeight() / (bufferedImage.getWidth() / scaledWidth);
            } else if(this.width != -1) {
                scaledHeight = this.height;
                scaledWidth = this.width;
            } else {
                scaledHeight = bufferedImage.getHeight();
                scaledWidth = bufferedImage.getWidth();
            }

            bufferedImage = ImageHelper.scale(bufferedImage, scaledWidth, scaledHeight);
            this.saveFile(bufferedImage, this.images.get(i).getTitle() + "_" + scaledWidth + "_" +  scaledHeight);
            this.updateProgress(i + 1, max);
        }

        this.updateProgress(max, max);
        return null;
    }

    private void saveFile(BufferedImage bufferedImage, String name) throws Exception {
        File file = new File(this.directory.getPath() + File.separatorChar + name + ".jpg");
        ImageIO.write(bufferedImage, "jpeg", file);
        Image image = new Image();
        image.setPath(file.getAbsolutePath());
        image.setDirectory(this.directory);
        image.setHeight(bufferedImage.getWidth());
        image.setWidth(bufferedImage.getHeight());
        image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(ImageHelper.getImage(image.getPath()), 50, 50)));
        image.setTitle(name);
        PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
    }
}
