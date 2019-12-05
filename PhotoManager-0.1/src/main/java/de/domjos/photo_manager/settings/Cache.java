package de.domjos.photo_manager.settings;

import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class Cache {
    private Image original;
    private BufferedImage previewImage, originalPreview;

    public Cache() {
        this.original = null;
        this.previewImage = null;
        this.originalPreview = null;
    }

    public Image getOriginal() {
        return this.original;
    }

    public void setOriginal(Image original) {
        this.original = original;
    }

    public BufferedImage getPreviewImage() {
        return this.previewImage;
    }

    public void setPreviewImage(BufferedImage previewImage) {
        this.previewImage = previewImage;
    }

    public BufferedImage getOriginalPreview() {
        return this.originalPreview;
    }

    public void setOriginalPreview(BufferedImage originalPreview) {
        this.originalPreview = originalPreview;
    }
}
