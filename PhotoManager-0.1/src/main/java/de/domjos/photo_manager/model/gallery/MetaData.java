package de.domjos.photo_manager.model.gallery;

public class MetaData {
    private double longitude;
    private double latitude;
    private int iso;
    private String original;
    private int xResolutionInDPI;
    private int yResolutionInDPI;
    private int xResolutionInPX;
    private int yResolutionInPX;
    private String editedWith;
    private int aperture;
    private String exposureTime;
    private String camera;

    public MetaData() {

    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getIso() {
        return this.iso;
    }

    public void setIso(int iso) {
        this.iso = iso;
    }

    public String getOriginal() {
        return this.original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public int getXResolutionInDPI() {
        return this.xResolutionInDPI;
    }

    public void setXResolutionInDPI(int xResolutionInDPI) {
        this.xResolutionInDPI = xResolutionInDPI;
    }

    public int getYResolutionInDPI() {
        return this.yResolutionInDPI;
    }

    public void setYResolutionInDPI(int yResolutionInDPI) {
        this.yResolutionInDPI = yResolutionInDPI;
    }

    public int getXResolutionInPX() {
        return this.xResolutionInPX;
    }

    public void setXResolutionInPX(int xResolutionInPX) {
        this.xResolutionInPX = xResolutionInPX;
    }

    public int getYResolutionInPX() {
        return this.yResolutionInPX;
    }

    public void setYResolutionInPX(int yResolutionInPX) {
        this.yResolutionInPX = yResolutionInPX;
    }

    public String getEditedWith() {
        return this.editedWith;
    }

    public void setEditedWith(String editedWith) {
        this.editedWith = editedWith;
    }

    public int getAperture() {
        return aperture;
    }

    public void setAperture(int aperture) {
        this.aperture = aperture;
    }

    public String getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(String exposureTime) {
        this.exposureTime = exposureTime;
    }

    public String getCamera() {
        return this.camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }
}
