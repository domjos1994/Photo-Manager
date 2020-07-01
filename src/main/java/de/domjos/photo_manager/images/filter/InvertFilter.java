package de.domjos.photo_manager.images.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;

public final class InvertFilter extends Filter {

    @Override
    public BufferedImage processImage(BufferedImage image) {
        if(image!=null) {
            byte[] invertArray = new byte[256];

            for (int counter = 0; counter < 256; counter++)
                invertArray[counter] = (byte) (255 - counter);

            BufferedImageOp invertFilter = new LookupOp(new ByteLookupTable(0, invertArray), null);
            return invertFilter.filter(image, null);
        }
        return null;
    }
}
