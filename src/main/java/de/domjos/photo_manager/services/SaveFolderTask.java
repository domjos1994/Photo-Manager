package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Directory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public final class SaveFolderTask extends ParentTask<Void> {
    private String message;
    private Directory directory;
    private long parent;
    private boolean recursive;
    public IntegerProperty counter = new SimpleIntegerProperty(0);
    public int max = 0;

    public SaveFolderTask(ProgressBar progressBar, Label messages, String message, Directory directory, long parent, boolean recursive) {
        super(progressBar, messages);

        this.message = message;
        this.directory = directory;
        this.parent = parent;
        this.recursive = recursive;

        this.counter.addListener((observableValue, number, t1) -> updateProgress(t1.intValue(), this.max));
    }

    @Override
    Void runBody() throws Exception {
        updateMessage(this.message);
        updateProgress(0, 1);
        PhotoManager.GLOBALS.getDatabase().insertOrUpdateDirectory(this.directory, this.parent, this.recursive, this);
        updateProgress(1, 1);
        updateMessage("");
        return null;
    }
}
