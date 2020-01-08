package de.domjos.photo_manager.model.gallery;

import de.domjos.photo_manager.controller.SettingsController;

public class Folder extends Directory {
    private String icon;
    private SettingsController.DirRow dirRow;

    public Folder() {
        super();

        this.dirRow = null;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public SettingsController.DirRow getDirRow() {
        return this.dirRow;
    }

    public void setDirRow(SettingsController.DirRow dirRow) {
        this.dirRow = dirRow;
    }
}
