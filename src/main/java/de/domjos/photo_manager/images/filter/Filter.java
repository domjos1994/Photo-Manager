package de.domjos.photo_manager.images.filter;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class Filter {
    protected final int NO_OP = ConvolveOp.EDGE_NO_OP;
    public static Map<Type, Filter> filters = new LinkedHashMap<>();

    static {
        filters.put(Type.ColorFilter, new ColorFilter());
        filters.put(Type.InvertFilter, new InvertFilter());
        filters.put(Type.SharpenFilter, new SharpenFilter());
        filters.put(Type.BlurFilter, new BlurFilter());
        filters.put(Type.GaussFilter, new GaussFilter());
        filters.put(Type.ReliefFilter, new ReliefFilter());
    }

    public abstract BufferedImage processImage(BufferedImage image);

    protected Kernel get3x3Kernel(float[] matrix) {
        return new Kernel(3, 3, matrix);
    }

    public enum Type {
        ColorFilter,
        InvertFilter,
        SharpenFilter,
        BlurFilter,
        GaussFilter,
        ReliefFilter
    }
}

