package de.domjos.photo_manager.model.services;

import de.domjos.photo_manager.model.gallery.DescriptionObject;

public class Cloud extends DescriptionObject {
    private String path;

    public Cloud() {
        super();
        this.path = "";
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
