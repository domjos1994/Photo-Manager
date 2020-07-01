package de.domjos.photo_manager.images.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;

public final class GaussFilter extends Filter {

    @Override
    public BufferedImage processImage(BufferedImage image) {
        if(image != null) {
            float[] gaussMatrix = {
                    1.0f / 9.0f, 2.0f / 9.0f, 1.0f / 9.0f,
                    2.0f / 9.0f, 4.0f / 9.0f, 2.0f / 9.0f,
                    1.0f / 9.0f, 2.0f / 9.0f, 1.0f / 9.0f
            };

            BufferedImageOp gaussFilter = new ConvolveOp(super.get3x3Kernel(gaussMatrix), super.NO_OP, null);
            return gaussFilter.filter(image, null);
        }
        return null;
    }
}
