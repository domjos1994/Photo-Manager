package de.domjos.photo_manager.model.gallery;

import de.domjos.photo_manager.model.objects.BaseObject;

public class Folder extends BaseObject {
    private String icon;
    private BatchTemplate batchTemplate;
    private Directory directory;

    public Folder() {
        super();

        this.icon = "";
        this.batchTemplate = null;
        this.directory = null;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public BatchTemplate getBatchTemplate() {
        return this.batchTemplate;
    }

    public void setBatchTemplate(BatchTemplate batchTemplate) {
        this.batchTemplate = batchTemplate;
    }

    public Directory getDirectory() {
        return this.directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }
}
