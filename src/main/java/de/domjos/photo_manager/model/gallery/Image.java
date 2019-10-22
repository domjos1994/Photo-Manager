package de.domjos.photo_manager.model.gallery;

import de.domjos.photo_manager.model.objects.BaseObject;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.model.services.Cloud;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class Image extends BaseObject {
    private String path;
    private byte[] thumbnail;
    private int width;
    private int height;
    private BufferedImage bufferedImage;
    private Directory directory;
    private DescriptionObject category;
    private List<DescriptionObject> tags;
    private List<TemporaryEdited> temporaryEditedList;
    private Cloud cloud;

    public Image() {
        super();

        this.path = "";
        this.thumbnail = null;
        this.directory = null;
        this.width = 0;
        this.height = 0;
        this.tags = new LinkedList<>();
        this.temporaryEditedList = new LinkedList<>();
        this.cloud = null;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Directory getDirectory() {
        return this.directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public DescriptionObject getCategory() {
        return this.category;
    }

    public void setCategory(DescriptionObject category) {
        this.category = category;
    }

    public List<DescriptionObject> getTags() {
        return this.tags;
    }

    public void setTags(List<DescriptionObject> tags) {
        this.tags = tags;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<TemporaryEdited> getTemporaryEditedList() {
        return this.temporaryEditedList;
    }

    public void setTemporaryEditedList(List<TemporaryEdited> temporaryEditedList) {
        this.temporaryEditedList = temporaryEditedList;
    }

    public Cloud getCloud() {
        return this.cloud;
    }

    public void setCloud(Cloud cloud) {
        this.cloud = cloud;
    }
}
