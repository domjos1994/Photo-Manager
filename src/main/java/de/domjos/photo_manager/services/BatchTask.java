package de.domjos.photo_manager.services;

import com.tinify.Source;
import com.tinify.Tinify;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.BatchTemplate;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.settings.Globals;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class BatchTask extends ParentTask<Void> {
    private final List<Image> images;
    private final BatchTemplate batchTemplate;

    public BatchTask(ProgressBar progressBar, Label messages, BatchTemplate batchTemplate, List<Image> images) {
        super(progressBar, messages);

        this.batchTemplate = batchTemplate;
        this.images = images;
    }

    @Override
    Void runBody() throws Exception {
        int current = 1, max = this.images.size();
        for(Image image : this.images) {
            this.updateMessage(image.getTitle());

            BufferedImage bufferedImage = ImageHelper.getImage(image.getPath());
            bufferedImage = this.resizeImage(bufferedImage);
            bufferedImage = this.compress(bufferedImage);
            image.setTitle(this.getName(image, current));
            this.createImage(image, bufferedImage);
            this.uploadToFTP(image, bufferedImage);

            this.updateProgress(current, max);
            current++;
        }

        return null;
    }

    private BufferedImage resizeImage(BufferedImage image) {
        try {
            if(this.batchTemplate.getWidth() != -1 || this.batchTemplate.getHeight() != -1) {
                int width, height;
                if(this.batchTemplate.getWidth() != -1 && this.batchTemplate.getHeight() != -1) {
                    width = this.batchTemplate.getWidth();
                    height = this.batchTemplate.getHeight();
                } else if(this.batchTemplate.getWidth() != -1) {
                    width = this.batchTemplate.getWidth();
                    double wFactor = (double) image.getWidth() / width;
                    height = (int) (image.getHeight() / wFactor);
                } else {
                    height = this.batchTemplate.getHeight();
                    double hFactor = (double) image.getHeight() / height;
                    width = (int) (image.getWidth() / hFactor);
                }

                return ImageHelper.scale(image, width, height);
            }
        } catch (Exception ex) {
            this.updateMessage(ex.getMessage());
        }
        return image;
    }

    private BufferedImage compress(BufferedImage image) {
        try {
            if(this.batchTemplate.isCompress()) {
                Tinify.setKey(PhotoManager.GLOBALS.getDecryptedSetting(Globals.TINY_KEY, ""));
                Source source = Tinify.fromBuffer(ImageHelper.imageToByteArray(image));
                return ImageIO.read(new ByteArrayInputStream(source.toBuffer()));
            }
        } catch (Exception ex) {
            this.updateMessage(ex.getMessage());
        }
        return image;
    }

    private String getName(Image image, int index) {
        if(!this.batchTemplate.getRename().trim().isEmpty()) {
            return this.batchTemplate.getRename().trim().replace("#", String.valueOf(index)).replace("$", image.getTitle());
        }
        return image.getTitle();
    }

    private void createImage(Image image, BufferedImage bufferedImage) {
        try {
            String ext = FilenameUtils.getExtension(image.getPath());
            String path;
            Image tmp;
            if(this.batchTemplate.isFolder()) {
                path = this.batchTemplate.getTargetFolder().getPath() + File.separatorChar + image.getTitle() + "." + ext;
                ImageHelper.save(image.getPath(), path, bufferedImage);

                Image newImage = new Image();
                newImage.setPath(path);
                newImage.setDirectory(this.batchTemplate.getTargetFolder());
                newImage.setTitle(image.getTitle());
                newImage.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(bufferedImage, 50, 50)));
                newImage.setHeight(bufferedImage.getHeight());
                newImage.setWidth(bufferedImage.getWidth());
                tmp = newImage;
            } else {
                path = FilenameUtils.getPath(image.getPath()) + File.separatorChar + image.getTitle() + "." + ext;
                ImageHelper.save(image.getPath(), path, bufferedImage);
                image.setPath(path);
                image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(bufferedImage, 50, 50)));
                tmp = image;
            }
            PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(tmp);
        } catch (Exception ex) {
            this.updateMessage(ex.getMessage());
        }
    }

    private void uploadToFTP(Image image, BufferedImage bufferedImage) {
        try {
            if(this.batchTemplate.isFtp()) {
                FTPClient client = new FTPClient();
                int port = getPort(this.batchTemplate);
                client.connect(this.batchTemplate.getServer().trim().split(":")[0], port);
                client.login(this.batchTemplate.getUser(), this.batchTemplate.getPassword());
                if(this.batchTemplate.getTargetFolderFtp() != null) {
                    client.changeWorkingDirectory(this.batchTemplate.getTargetFolderFtp().getPath());

                    String ext = FilenameUtils.getExtension(image.getPath());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, ext, os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                    client.appendFile(image.getTitle() + "." + ext, is);
                }
            }
        } catch (Exception ex) {
            this.updateMessage(ex.getMessage());
        }
    }

    public static void addFoldersToTreeView(BatchTemplate batchTemplate, TreeItem<Directory> treeItem) throws Exception {
        FTPClient client = new FTPClient();
        int port = getPort(batchTemplate);
        client.connect(batchTemplate.getServer().trim().split(":")[0], port);
        client.login(batchTemplate.getUser(), batchTemplate.getPassword());
        client.changeWorkingDirectory(treeItem.getValue().getPath());

        BatchTask.addItems(treeItem, client.listDirectories(), client);
    }

    public static ImageView getFolderIcon() {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        imageView.setImage(new javafx.scene.image.Image(BatchTask.class.getResourceAsStream("/images/icons/folder.png")));
        return imageView;
    }

    private static void addItems(TreeItem<Directory> treeItem, FTPFile[] files, FTPClient client) throws Exception {
        for(FTPFile file : files) {
            if(!file.getName().equals(".") && !file.getName().equals("..")) {
                Directory directory = new Directory();
                directory.setPath(treeItem.getValue().getPath() + file.getName() + "/");
                directory.setTitle(file.getName());

                TreeItem<Directory> sub = new TreeItem<>(directory, BatchTask.getFolderIcon());
                treeItem.getChildren().add(sub);

                client.changeWorkingDirectory(file.getName());
            }
        }
    }

    private static int getPort(BatchTemplate batchTemplate) {
        int port;
        if(batchTemplate.isFtpSecure()) {
            port = 22;
        } else {
            port = 21;
        }
        if(!batchTemplate.getServer().trim().isEmpty()) {
            if(batchTemplate.getServer().trim().contains(":")) {
                port = Integer.parseInt(batchTemplate.getServer().trim().split(":")[1]);
            }
        }
        return port;
    }
}
