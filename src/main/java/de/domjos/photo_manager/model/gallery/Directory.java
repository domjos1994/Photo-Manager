package de.domjos.photo_manager.model.gallery;

import java.util.LinkedList;
import java.util.List;

public class Directory {
    private long id;
    private String name;
    private String path;
    private boolean root;
    private boolean library;
    private boolean recursive;
    private List<Directory> children;
    private List<Image> images;

    public Directory() {
        this.id = 0;
        this.name = "";
        this.path = "";
        this.root = false;
        this.library = false;
        this.recursive = false;
        this.children = new LinkedList<>();
        this.images = new LinkedList<>();
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

    public boolean isRoot() {
        return this.root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public List<Directory> getChildren() {
        return this.children;
    }

    public void setChildren(List<Directory> children) {
        this.children = children;
    }

    public List<Image> getImages() {
        return this.images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String toString() {
        return this.name;
    }

    public boolean isLibrary() {
        return this.library;
    }

    public void setLibrary(boolean library) {
        this.library = library;
    }

    public boolean isRecursive() {
        return this.recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
}
