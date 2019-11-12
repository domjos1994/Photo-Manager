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
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;

public class ImageHelper {
    private final static String[] EXTENSIONS = new String[]{"gif", "png", "bmp", "PNG", "jpg", "jpeg", "JPG"};

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

    public static BufferedImage rotate(BufferedImage bufferedImage, int rotation) {
        double rotationRequired = Math.toRadians (rotation);
        double locationX = bufferedImage.getWidth() / 2.0;
        double locationY = bufferedImage.getHeight() / 2.0;
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage newImage =new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        op.filter(bufferedImage, newImage);
        return(newImage);
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static boolean checkImageHasAlpha(BufferedImage bufferedImage) {
        return bufferedImage.getColorModel().hasAlpha();
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
}
