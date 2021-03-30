package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.MainController;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Folder;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.services.CreateAssembledImage;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.scene.control.Alert;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Execute PhotoManager-Functions via Commandline-Arguments
 * save <Directory or File> <Name of PhotoManager-Directory>
 * batch <Directory or File>
 * assemble <Directory or File> <Path to Image> <width> <height> <scaled height>
 */
public class ArgsHelper {
    private ArgType argType;
    private File path;
    private List<File> files;

    private final String[] arguments;
    private final MainController mainController;


    public ArgsHelper(String[] args, MainController mainController) {
        this.mainController = mainController;
        this.arguments = args;
        if(this.check()) {
            this.doActions();
        }
    }

    private void doActions() {
        switch (this.argType) {
            case save:
                for(File current : this.files) {
                    if (arguments.length == 3) {
                        String folderName = arguments[2].trim();
                        Directory directory = this.mainController.findDirectory(this.mainController.getTvMain().getRoot(), folderName);
                        if (directory != null) {
                            try {
                                String path = directory.getPath();

                                File newFile = new File(path + File.separatorChar + current.getName());
                                FileInputStream fis = new FileInputStream(current);
                                if (newFile.exists() || newFile.createNewFile()) {
                                    FileOutputStream fos = new FileOutputStream(newFile);
                                    IOUtils.copy(fis, fos);
                                    fis.close();
                                    fos.close();
                                    this.mainController.saveImage(newFile, directory);
                                }

                            } catch (Exception ex) {
                                Dialogs.printException(ex);
                            }
                        }
                    }
                }
                break;
            case batch:
                List<Image> images = new LinkedList<>();
                for(File file : this.files)    {
                    String newFile = this.path.getAbsolutePath() + File.separatorChar + file.getName();
                    try {
                        BufferedImage bufferedImage = ImageHelper.getImage(newFile);
                        if(bufferedImage != null) {
                            Image image = new Image();
                            image.setHeight(bufferedImage.getHeight());
                            image.setWidth(bufferedImage.getWidth());
                            image.setPath(newFile);
                            image.setTitle(new File(newFile).getName().split("\\.")[0].trim());
                            image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(bufferedImage, 128, 128)));
                            images.add(image);
                        }
                    } catch (Exception ignored) {}
                }
                this.mainController.openBatchWindow(images);
                break;
            case assemble:
                Dialogs.AssembleResult assembleResult = new Dialogs.AssembleResult();
                assembleResult.scaledHeight = Integer.parseInt(arguments[5]);
                assembleResult.width = Integer.parseInt(arguments[3]);
                assembleResult.height = Integer.parseInt(arguments[4]);
                String path = arguments[2];

                List<Image> imageList = new LinkedList<>();
                for(File file : this.files) {
                    try {
                        BufferedImage bufferedImage = ImageIO.read(file);
                        Image image = new Image();
                        image.setPath(file.getAbsolutePath());
                        image.setWidth(bufferedImage.getWidth());
                        image.setHeight(bufferedImage.getHeight());
                        imageList.add(image);
                    } catch (Exception ignored) {}
                }

                CreateAssembledImage createAssembledImage = new CreateAssembledImage(this.mainController.getProgressBar(), this.mainController.getMessages(), assembleResult, path, imageList);
                new Thread(createAssembledImage).start();
                break;
        }
    }

    private boolean check() {
        if(this.arguments == null) {
            this.writeMsg("arguments.validation.errorReading");
            return false;
        }
        if(this.arguments.length <= 1) {
            this.writeMsg("arguments.validation.numberArguments");
            return false;
        } else {
            try {
                this.argType = ArgType.valueOf(this.arguments[0]);
            } catch (Exception ex) {
                this.writeMsg("arguments.validation.unknown");
                return false;
            }

            File file = new File(this.arguments[1]);
            if(file.exists()) {
                this.path = file;
                this.files = this.listImageFiles(file.getAbsolutePath());
                switch (this.argType) {
                    case save:
                        if(this.arguments.length != 3) {
                            this.writeMsg("arguments.validation.numberArguments");
                            return false;
                        }
                        break;
                    case assemble:
                        if(this.arguments.length != 6) {
                            this.writeMsg("arguments.validation.numberArguments");
                            return false;
                        }
                        File assembleLocation = new File(this.arguments[2]);
                        if(assembleLocation.exists()) {
                            this.writeMsg("arguments.validation.exists.not");
                            return false;
                        }
                        try {
                            Integer.parseInt(this.arguments[3]);
                        } catch (Exception ex) {
                            this.writeMsg("arguments.validation.errorReading");
                            return false;
                        }
                        try {
                            Integer.parseInt(this.arguments[4]);
                        } catch (Exception ex) {
                            this.writeMsg("arguments.validation.errorReading");
                            return false;
                        }

                        try {
                            Integer.parseInt(this.arguments[5]);
                        } catch (Exception ex) {
                            this.writeMsg("arguments.validation.errorReading");
                            return false;
                        }
                }
            } else {
                this.writeMsg("arguments.validation.exists");
                return false;
            }
        }

        return true;
    }

    private void writeMsg(String key) {
        Dialogs.printNotification(Alert.AlertType.ERROR, PhotoManager.GLOBALS.getLanguage().getString(key),PhotoManager.GLOBALS.getLanguage().getString(key));
    }

    private List<File> listImageFiles(String dir) {
        List<File> files = new LinkedList<>();
        File file = new File(dir);
        if(file.exists()) {
            if(file.isDirectory()) {
                files.addAll(Arrays.asList(Objects.requireNonNull(file.listFiles(ImageHelper.getFilter()))));
            } else if(file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }

    public enum ArgType {
        batch,
        save,
        assemble
    }
}
