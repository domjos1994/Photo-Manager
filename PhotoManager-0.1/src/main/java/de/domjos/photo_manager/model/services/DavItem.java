package de.domjos.photo_manager.model.services;

import com.github.sardine.DavResource;

public class DavItem {
    private DavResource davResource;

    public DavItem(DavResource davResource) {
        this.davResource = davResource;
    }

    public DavResource get() {
        return this.davResource;
    }

    @Override
    public String toString() {
        return this.davResource.getName();
    }
}
