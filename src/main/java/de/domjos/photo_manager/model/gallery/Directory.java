package de.domjos.photo_manager.model.gallery;

import de.domjos.photo_manager.model.objects.BaseObject;
import de.domjos.photo_manager.model.services.Cloud;

import java.util.LinkedList;
import java.util.List;

public class Directory extends BaseObject {
    private String path;
    private boolean root;
    private boolean library;
    private boolean recursive;
    private List<Directory> children;
    private List<Image> images;
    private Cloud cloud;
    private Folder folder;

    public Directory() {
        super();

        this.path = "";
        this.root = false;
        this.library = false;
        this.recursive = false;
        this.children = new LinkedList<>();
        this.images = new LinkedList<>();
        this.cloud = null;
        this.folder = null;
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

    public Cloud getCloud() {
        return this.cloud;
    }

    public void setCloud(Cloud cloud) {
        this.cloud = cloud;
    }

    public Folder getFolder() {
        return this.folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String toString() {
        return super.getTitle();
    }
}
