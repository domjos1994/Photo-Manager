package de.domjos.photo_manager.model.gallery;

public class Folder extends Directory {
    private String icon;

    public Folder() {
        super();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
