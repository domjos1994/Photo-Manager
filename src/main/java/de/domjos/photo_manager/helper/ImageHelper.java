package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.model.gallery.MetaData;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;

/**
 * Class which contains helper and useful functions
 * for the images!
 * @author Dominic Joas
 * @version 0.1
 */
public class ImageHelper {
    public final static String[] EXTENSIONS = new String[]{"gif", "png", "bmp", "PNG", "jpg", "jpeg", "JPG"};

    /**
     * Returns Supported Image-Extension-Filters
     * @return FileNameFilter
     */
    public static FilenameFilter getFilter() {
        return  (dir, name) -> {
            for (final String ext : ImageHelper.EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        };
    }

    public static BufferedImage getImage(String path) throws Exception {
        ImageReader imageReader = ImageHelper.getImageReader(path);
        if(imageReader!=null) {
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(new File(path));
            imageReader.setInput(imageInputStream, true);
            ImageReadParam param = imageReader.getDefaultReadParam();
            return imageReader.read(0, param);
        }
        return null;
    }

    public static BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }

    public static byte[] imageToByteArray(BufferedImage bufferedImage) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if(ImageHelper.checkImageHasAlpha(bufferedImage)) {
            ImageIO.write(bufferedImage, "png", bos);
        } else {
            ImageIO.write(bufferedImage, "jpg", bos);
        }
        return bos.toByteArray();
    }

    public static int[] getHistogram(BufferedImage bufferedImage, int type) {
        if(bufferedImage!=null) {
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            int[] histogramArray = new int[256];

            if(type==0) {
                return getHistogram(bufferedImage);
            } else {
                Raster raster = bufferedImage.getRaster();
                for(int y = 0; y<=height-1; y++) {
                    for(int x = 0; x<=width-1; x++) {
                        histogramArray[raster.getSample(x, y, type-1)]++;
                    }
                }
            }
            return histogramArray;
        }
        return null;
    }

    public static MetaData readMetaData(String path) throws Exception {
        MetaData metaData = new MetaData();
        ImageMetadata metadata = Imaging.getMetadata(new File(path));
        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            // find GPS-Data
            try {
                final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                if (null != exifMetadata) {
                    final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                    if (null != gpsInfo) {
                        final double longitude = gpsInfo.getLongitudeAsDegreesEast();
                        final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

                        metaData.setLongitude(longitude);
                        metaData.setLatitude(latitude);
                    }
                }
            } catch (Exception ignored) {}

            // find original Date
            try {
                Object date = ImageHelper.getValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
                if(date!=null) {
                    metaData.setOriginal(date.toString());
                }
            } catch (Exception ignored) {}

            // find DPI-Resolution-Data
            try {
                Object xDPI = ImageHelper.getValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION);
                if(xDPI!=null) {
                    metaData.setXResolutionInDPI(Integer.parseInt(String.valueOf(xDPI)));
                }
                Object yDPI = ImageHelper.getValue(jpegMetadata, TiffTagConstants.TIFF_TAG_YRESOLUTION);
                if(yDPI!=null) {
                    metaData.setYResolutionInDPI(Integer.parseInt(String.valueOf(yDPI)));
                }
            } catch (Exception ignored) {}

            // find PX-Resolution-Data
            try {
                BufferedImage bufferedImage = ImageHelper.getImage(path);
                if(bufferedImage!=null) {
                    metaData.setXResolutionInPX(bufferedImage.getWidth());
                    metaData.setYResolutionInPX(bufferedImage.getHeight());
                }
            } catch (Exception ignored) {}

            // find EXIF-Data
            try {
                Object iso = ImageHelper.getValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO);
                if(iso!=null) {
                    metaData.setIso(Integer.parseInt(String.valueOf(iso)));
                }
                Object software = ImageHelper.getValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SOFTWARE);
                if(software!=null) {
                    metaData.setEditedWith(String.valueOf(software));
                }
                Object aperture = ImageHelper.getValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FNUMBER);
                if(aperture!=null) {
                    metaData.setAperture(Integer.parseInt(String.valueOf(aperture)));
                }
                Object exposureTime = ImageHelper.getValue(jpegMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_TIME);
                if(exposureTime!=null) {
                    metaData.setExposureTime(String.valueOf(exposureTime));
                }

                try {
                    StringBuilder model = new StringBuilder();
                    final java.util.List<ImageMetadata.ImageMetadataItem> items = jpegMetadata.getItems();
                    for (final ImageMetadata.ImageMetadataItem item : items) {
                        if (item.toString().startsWith("Make: ")) {
                            model.append(item.toString().replace("Make: ", "").replace("'", "").trim());
                            model.append(" ");
                        }
                        if (item.toString().startsWith("Model: ")) {
                            model.append(item.toString().replace("Model: ", "").replace("'", "").replace(model.toString().trim(), "").trim());
                        }
                    }
                    metaData.setCamera(model.toString());
                } catch (Exception ex) {
                    System.err.println(ex.toString());
                }
            } catch (Exception ignored) {}
        }
        return metaData;
    }

    public static void changeHSB(BufferedImage bufferedImage, BufferedImage original, int hue, int saturation, int brightness) {
        if(bufferedImage!=null) {
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            for(int y = 0; y<=height-1; y++) {
                for(int x = 0; x<=width-1; x++) {
                    Color color = new Color(original.getRGB(x, y));
                    float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                    hsb[0] = ((hsb[0]/100f) * hue);
                    hsb[1] = ((hsb[1]/100f) * saturation);
                    hsb[2] = ((hsb[2]/100f) * brightness);
                    int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                    bufferedImage.setRGB(x, y, rgb);
                }
            }
        }
    }

    public static BufferedImage rotate(BufferedImage img, int degrees) {
        if(img != null) {
            double rads = Math.toRadians(degrees);
            int w = img.getWidth();
            int h = img.getHeight();

            BufferedImage rotated = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = rotated.createGraphics();
            AffineTransform at = new AffineTransform();

            int x = w / 2;
            int y = h / 2;

            at.rotate(rads, x, y);
            g2d.setTransform(at);
            g2d.drawImage(img, 0, 0, null);
            g2d.setColor(Color.RED);
            g2d.drawRect(0, 0, w - 1, h - 1);
            g2d.dispose();

            return rotated;
        }
        return null;
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPreMultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPreMultiplied, null);
    }

    public static BufferedImage addWaterMark(BufferedImage bufferedImage, String text) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int imageType = ImageHelper.checkImageHasAlpha(bufferedImage) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage watermarked = new BufferedImage(width, height, imageType);

        Graphics2D w = (Graphics2D) watermarked.getGraphics();
        w.drawImage(bufferedImage, 0, 0, null);
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
        w.setComposite(alphaChannel);
        w.setColor(Color.GRAY);
        w.setFont(new Font(Font.SANS_SERIF, Font.BOLD, height / 25));
        w.drawString(text, width / 25, height - (height / 25));
        return watermarked;
    }

    public static BufferedImage resize(BufferedImage img, javafx.scene.image.Image original, int newW, int newH) {
        if(newW == 0 || newH == 0) {
            return img;
        } else {
            double wFactor = original.getWidth() / newW;
            double hFactor = original.getHeight() / newH;
            int newWidth = (int) (img.getWidth() / wFactor);
            int newHeight = (int) (img.getHeight() / hFactor);

            Image tmp = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage newImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = newImg.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();

            return newImg;
        }
    }

    public static BufferedImage addFilter(BufferedImage bufferedImage, Filter.Type type) {
        Filter filter;
        switch (type) {
            case ColorFilter:
                filter = new ColorFilter();
                break;
            case InvertFilter:
                filter = new InvertFilter();
                break;
            case SharpenFilter:
                filter = new SharpenFilter();
                break;
            case BlurFilter:
                filter = new BlurFilter();
                break;
            default:
                filter = null;
                break;
        }
        return filter.processImage(bufferedImage);
    }

    public static void save(String path, String save, BufferedImage bufferedImage) throws Exception {
        ImageInputStream iis = ImageIO.createImageInputStream(new File(path));
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);

        if(imageReaders.hasNext()) {
            ImageReader reader = imageReaders.next();
            ImageWriter imageWriter = ImageIO.getImageWriter(reader);
            imageWriter.setOutput(new FileImageOutputStream(new File(save)));
            bufferedImage = ImageHelper.convertColorSpace(bufferedImage);
            imageWriter.write(bufferedImage);
        }
    }

    private static ImageReader getImageReader(String path) {
        for(String extension : ImageHelper.EXTENSIONS) {
            if(path.endsWith(extension)) {
                Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName(extension);
                if ( imageReaders.hasNext() ) {
                    return imageReaders.next();
                }
            }
        }
        return null;
    }

    private static BufferedImage convertColorSpace(BufferedImage image) {
        BufferedImage raw_image = image;
        image = new BufferedImage(raw_image.getWidth(), raw_image.getHeight(), BufferedImage.TYPE_INT_RGB);
        ColorConvertOp xFormOp = new ColorConvertOp(null);
        xFormOp.filter(raw_image, image);
        return image;
    }

    private static Object getValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) throws Exception {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field != null) {
            return field.getValue();
        }
        return null;
    }

    private static int[] getHistogram(BufferedImage bufferedImage) {
        int [] histogram;
        histogram = new int[256];

        int[][] pixels = getPixels(bufferedImage);
        for (int[] pixel : pixels) {
            for (int y = 0; y < pixels[0].length; y++) {
                histogram[pixel[y]]++;
            }
        }
        return histogram;
    }

    private static int[][] getPixels(BufferedImage bufferedImage) {
        if(bufferedImage!=null) {
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            int[][] pixels = new int[width][height];

            DataBufferByte db = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
            byte[] pixelArray = db.getData();
            for (int x = 0; x < width; x++ ) {
                for (int y = 0; y < height; y++ ) {
                    pixels[x][y] = pixelArray[x + y * width] &0xFF;
                }
            }
            return pixels;
        }
        return null;
    }

    private static boolean checkImageHasAlpha(BufferedImage bufferedImage) {
        return bufferedImage.getColorModel().hasAlpha();
    }

    public interface Filter {
        BufferedImage processImage(BufferedImage image);

        enum Type {
            ColorFilter,
            InvertFilter,
            SharpenFilter,
            BlurFilter
        }
    }

    private static class ColorFilter implements Filter {
        public BufferedImage processImage(BufferedImage image) {
            float[][] colorMatrix = { { 1f, 0f, 0f, 1f }, { 0.5f, 1.0f, 0.5f, 1f }, { 0.2f, 0.4f, 0.6f, 1f }, {1f, 1f, 1f, 1f}};
            BandCombineOp changeColors = new BandCombineOp(colorMatrix, null);
            Raster sourceRaster = image.getRaster();
            WritableRaster displayRaster = sourceRaster.createCompatibleWritableRaster();
            changeColors.filter(sourceRaster, displayRaster);
            return new BufferedImage(image.getColorModel(), displayRaster, true, null);
        }
    }

    private static class InvertFilter implements Filter {
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

    private static class SharpenFilter implements Filter {
        public BufferedImage processImage(BufferedImage image) {
            if(image!=null) {
                float[] sharpenMatrix = { 0.0f, -1.0f, 0.0f, -1.0f, 5.0f, -1.0f, 0.0f, -1.0f, 0.0f };
                BufferedImageOp sharpenFilter = new ConvolveOp(new Kernel(3, 3, sharpenMatrix),
                        ConvolveOp.EDGE_NO_OP, null);
                return sharpenFilter.filter(image, null);
            }
            return null;
        }
    }

    private static class BlurFilter implements Filter {
        public BufferedImage processImage(BufferedImage image) {
            if(image!=null) {
                float[] blurMatrix = { 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f,
                        1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f };
                BufferedImageOp blurFilter = new ConvolveOp(new Kernel(3, 3, blurMatrix),
                        ConvolveOp.EDGE_NO_OP, null);
                return blurFilter.filter(image, null);
            }
            return null;
        }
    }
}
