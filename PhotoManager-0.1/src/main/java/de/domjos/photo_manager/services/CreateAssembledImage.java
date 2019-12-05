package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class CreateAssembledImage extends ParentTask<Void> {
    private Dialogs.AssembleResult assembleResult;
    private Directory directory;
    private List<Image> images;
    private List<Integer> imageIndices;

    public CreateAssembledImage(ProgressBar progressBar, Label messages, Dialogs.AssembleResult assembleResult, Directory directory, List<Image> images) {
        super(progressBar, messages);
        this.assembleResult = assembleResult;
        this.directory = directory;
        this.imageIndices = new LinkedList<>();
        for(int i = 0; i<=images.size()-1; i++) {
            this.imageIndices.add(i);
        }
        Collections.shuffle(this.imageIndices);
        this.images = images;
    }

    @Override
    Void runBody() throws Exception {
        this.updateProgress(0, this.images.size() + 1);
        String name = this.assembleResult.name;

        int width = 0, height = 0, resizedHeight = 0;
        try {
            width = this.assembleResult.width;
            height = this.assembleResult.height;
            resizedHeight = this.assembleResult.scaledHeight;
        } catch (Exception ex) {
            this.cancel();
        }

        int imageCount = 1;
        int x = 0, y = 0, minY = 0;

        int calculatedHeight = 0;
        for(Image image : this.images) {
            if(calculatedHeight==0) {
                calculatedHeight = image.getHeight();
            } else if(calculatedHeight>=image.getHeight()) {
                calculatedHeight = image.getHeight();
            }
        }

        int wholeHeight = 0, wholeWidth = 0;
        while (height>wholeHeight) {
            wholeHeight = 0;
            for(Image image : this.images) {
                double factor = image.getHeight() / (double) calculatedHeight;
                int factorizedWidth = (int) (image.getWidth() / factor);
                wholeWidth += factorizedWidth;

                if(wholeWidth>=width) {
                    wholeHeight += calculatedHeight;
                    wholeWidth = 0;
                }
            }
            calculatedHeight++;
        }

        BufferedImage bufferedImage = ImageHelper.createImage(width, height);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        for(int index : this.imageIndices) {
            Image image = this.images.get(index);
            if(x >= width) {
                y += minY;
                x = 0;
                minY = 0;
            }
            BufferedImage current = ImageHelper.getImage(image.getPath());
            if(current!=null) {
                if(resizedHeight > 0) {
                    double factor = current.getHeight() / (double) resizedHeight;
                    current = ImageHelper.scale(current, (int) (current.getWidth() / factor), resizedHeight);
                } else if(resizedHeight == -1) {
                    double factor = current.getHeight() / (double) calculatedHeight;
                    current = ImageHelper.scale(current, (int) (current.getWidth() / factor), calculatedHeight);
                }

                graphics2D.drawImage(current, x, y, null);
                x += current.getWidth();
                if(minY > current.getHeight() || minY == 0) {
                    minY = current.getHeight();
                }
            }
            this.updateProgress(imageCount, this.images.size() + 1);
            imageCount++;
        }
        graphics2D.dispose();

        this.saveFile(bufferedImage, name);
        this.updateProgress(this.images.size() + 1, this.images.size() + 1);
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
