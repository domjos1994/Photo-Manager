package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public abstract class ParentTask<T> extends Task<T> {
    private Runnable onFinish, onFailed;

    ParentTask(ProgressBar progressBar, Label messages) {
        this.exceptionProperty().addListener((observable, oldValue, newValue) -> Dialogs.printException(newValue));

        if(progressBar!=null) {
            if(progressBar.progressProperty().isBound()) {
                progressBar.progressProperty().unbind();
            }
            progressBar.progressProperty().bind(this.progressProperty());
        }
        if(messages!=null) {
            if(messages.textProperty().isBound()) {
                messages.textProperty().unbind();
            }
            messages.textProperty().bind(this.messageProperty());
        }

        this.setOnSucceeded(event -> Platform.runLater(()->{
            if(this.onFinish!=null) {
                this.onFinish.run();
            }
            if(progressBar!=null) {
                if(progressBar.progressProperty().isBound()) {
                    progressBar.progressProperty().unbind();
                }
            }
            if(messages!=null) {
                if(messages.textProperty().isBound()) {
                    messages.textProperty().unbind();
                }
            }
            PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT);
        }));

        this.setOnFailed(event -> Platform.runLater(()->{
            if(this.onFailed!=null) {
                this.onFailed.run();
            }
            if(progressBar!=null) {
                if(progressBar.progressProperty().isBound()) {
                    progressBar.progressProperty().unbind();
                }
            }
            if(messages!=null) {
                if(messages.textProperty().isBound()) {
                    messages.textProperty().unbind();
                }
            }
            PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT);
        }));
        this.setOnCancelled(this.getOnFailed());
    }

    public T call() throws Exception {
        Platform.runLater(()-> PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.WAIT));
        T t = this.runBody();
        this.updateMessage("");
        this.updateProgress(0, 1);
        return t;
    }

    public void onFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }
    public void onFailed(Runnable onFailed) {
        this.onFailed = onFailed;
    }

    abstract T runBody() throws Exception;
}
