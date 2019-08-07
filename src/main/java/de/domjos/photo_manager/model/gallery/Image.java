package de.domjos.photo_manager.model.gallery;

import java.util.List;

public class Image {
    private long id;
    private String name;
    private String path;
    private byte[] thumbnail;
    private Directory directory;
    private DescriptionObject category;
    private List<DescriptionObject> tags;

    public Image() {
        this.id = 0;
        this.name = "";
        this.path = "";
        this.thumbnail = null;
        this.directory = null;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
}
