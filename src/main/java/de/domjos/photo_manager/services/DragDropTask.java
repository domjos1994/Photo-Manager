package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public final class DragDropTask extends ParentTask<Void> {
    private String content;
    private Directory directory;
    private ListView<Image> unsplash;

    public DragDropTask(ProgressBar progressBar, Label messages, String content, Directory directory, ListView<Image> unsplash) {
        super(progressBar, messages);
        this.content = content;
        this.directory = directory;
        this.unsplash = unsplash;
    }

    @Override
    Void runBody() throws Exception {
        String[] items = content.split(";");
        int min = 0, max = items.length;

        super.updateProgress(min, max);
        for(String item : items) {
            if(!item.trim().isEmpty()) {
                int index = Integer.parseInt(item.trim());
                Image image = this.unsplash.getItems().get(index);

                File file = new File(this.directory.getPath() + File.separatorChar + image.getExtended().getOrDefault("id", "tmp") + ".jpg");
                InputStream inputStream = new URL(image.getExtended().get("unSplash")).openStream();
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                inputStream.close();
                bufferedImage = ImageHelper.addWaterMark(bufferedImage, "(c) Unsplash");
                FileUtils.writeByteArrayToFile(file, ImageHelper.imageToByteArray(bufferedImage));
                image.setPath(file.getAbsolutePath());
                image.setDirectory(this.directory);
                PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
            }
            min++;
            updateProgress(min, max);
        }

        return null;
    }
}
