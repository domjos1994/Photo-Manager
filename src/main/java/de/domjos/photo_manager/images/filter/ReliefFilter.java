package de.domjos.photo_manager.images.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;

public final class ReliefFilter extends Filter {
    @Override
    public BufferedImage processImage(BufferedImage image) {
        if(image != null) {
            float[] laPlaceMatrix = {
                -2.0f, -1.0f, 0.0f,
                -1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 2.0f
            };

            BufferedImageOp laPlaceFilter = new ConvolveOp(super.get3x3Kernel(laPlaceMatrix), super.NO_OP, null);
            return laPlaceFilter.filter(image, null);
        }
        return null;
    }
}
