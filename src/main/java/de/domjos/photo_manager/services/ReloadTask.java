package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.utils.FileHelper;
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
        Directory directory = PhotoManager.GLOBALS.getDatabase().getDirectories("id=" + this.id, false).get(0);
        long countFiles = FileHelper.countFiles(directory.getPath(), true) * 2;
        updateProgress(countFiles, this.msg);
        Directory root = FileHelper.generateTree(directory.getPath(), true, this, countFiles);
        if(root != null) {
            SaveFolderTask.save(root, this.id, this, countFiles);
        }
        return null;
    }


}
