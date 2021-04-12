package de.domjos.photo_manager.services;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public final class CloudTask extends ParentTask<Boolean> {
    private final String serverName;
    private final String userName;
    private final String password;

    public CloudTask(ProgressBar progressBar, Label messages, String serverName, String userName, String password) {
        super(progressBar, messages);

        this.serverName = serverName;
        this.userName = userName;
        this.password = password;
    }

    @Override
    Boolean runBody() throws Exception {
        WebDav webDav = new WebDav(this.userName, this.password, this.serverName);

        return webDav.testConnection();
    }
}
