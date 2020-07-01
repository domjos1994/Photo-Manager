package de.domjos.photo_manager.images.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;

public final class SharpenFilter extends Filter {

    @Override
    public BufferedImage processImage(BufferedImage image) {
        if(image!=null) {
            float[] sharpenMatrix = {
                    0.0f, -1.0f, 0.0f,
                    -1.0f, 5.0f, -1.0f,
                    0.0f, -1.0f, 0.0f
            };

            BufferedImageOp sharpenFilter = new ConvolveOp(super.get3x3Kernel(sharpenMatrix), super.NO_OP, null);
            return sharpenFilter.filter(image, null);
        }
        return null;
    }
}
