package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Directory;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public final class ReloadTask extends ParentTask<Void> {
    private final long id;
    private final String msg;

    public ReloadTask(ProgressBar progressBar, Label messages, String msg, long id) {
        super(progressBar, messages);

        this.id = id;
        this.msg = msg;
    }

    @Override
    Void runBody() throws Exception {
        updateMessage(this.msg);
        updateProgress(0, 5);
        Directory directory = PhotoManager.GLOBALS.getDatabase().getDirectories("id=" + this.id, false).get(0);
        updateProgress(1, 5);
        Directory root = SaveFolderTask.generateTree(directory.getPath(), true);
        updateProgress(3, 5);
        if(root != null) {
            SaveFolderTask.save(root, this.id);
        }
        updateProgress(5, 5);
        updateMessage("");
        return null;
    }


}
