package de.domjos.photo_manager.images.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;

public final class BlurFilter extends Filter {

    @Override
    public BufferedImage processImage(BufferedImage image) {
        if(image!=null) {
            float[] blurMatrix = {
                    1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f,
                    1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f,
                    1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f
            };

            BufferedImageOp blurFilter = new ConvolveOp(super.get3x3Kernel(blurMatrix), super.NO_OP, null);
            return blurFilter.filter(image, null);
        }
        return null;
    }
}
