package de.domjos.photo_manager.images.filter;

import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class ColorFilter extends Filter {

    @Override
    public BufferedImage processImage(BufferedImage image) {
        float[][] colorMatrix = { { 1f, 0f, 0f, 1f }, { 0.5f, 1.0f, 0.5f, 1f }, { 0.2f, 0.4f, 0.6f, 1f }, {1f, 1f, 1f, 1f}};
        BandCombineOp changeColors = new BandCombineOp(colorMatrix, null);
        Raster sourceRaster = image.getRaster();
        WritableRaster displayRaster = sourceRaster.createCompatibleWritableRaster();
        changeColors.filter(sourceRaster, displayRaster);
        return new BufferedImage(image.getColorModel(), displayRaster, true, null);
    }
}
