package de.domjos.photo_manager.model.gallery;

import de.domjos.photo_manager.model.objects.BaseObject;

public class BatchTemplate extends BaseObject {
    private int width;
    private int height;
    private boolean compress;
    private boolean folder;
    private boolean ftp;
    private boolean ftpSecure;
    private String rename;
    private String server;
    private String user;
    private String password;
    private Directory targetFolder;
    private Directory targetFolderFtp;

    public BatchTemplate() {
        super();

        this.width = -1;
        this.height = -1;
        this.compress = false;
        this.folder = false;
        this.ftp = false;
        this.ftpSecure = false;
        this.rename = "";
        this.server = "";
        this.user = "";
        this.password = "";
        this.targetFolder = null;
        this.targetFolderFtp = null;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isCompress() {
        return this.compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isFolder() {
        return this.folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public boolean isFtp() {
        return this.ftp;
    }

    public void setFtp(boolean ftp) {
        this.ftp = ftp;
    }

    public boolean isFtpSecure() {
        return this.ftpSecure;
    }

    public void setFtpSecure(boolean ftpSecure) {
        this.ftpSecure = ftpSecure;
    }

    public String getRename() {
        return this.rename;
    }

    public void setRename(String rename) {
        this.rename = rename;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Directory getTargetFolder() {
        return this.targetFolder;
    }

    public void setTargetFolder(Directory targetFolder) {
        this.targetFolder = targetFolder;
    }

    public Directory getTargetFolderFtp() {
        return this.targetFolderFtp;
    }

    public void setTargetFolderFtp(Directory targetFolderFtp) {
        this.targetFolderFtp = targetFolderFtp;
    }

    @Override
    public String toString() {
        return super.getTitle();
    }
}
