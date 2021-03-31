package de.domjos.photo_manager.utils;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.services.ParentTask;
import de.domjos.photo_manager.services.SaveFolderTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileHelper {


    public static long countFiles(String filePath, boolean recursive) throws IOException {
        return FileHelper.countFiles(filePath, recursive, 0);
    }

    private static long countFiles(String filePath, boolean recursive, long current) throws IOException {
        Path dir = FileSystems.getDefault().getPath( filePath );
        DirectoryStream<Path> stream = Files.newDirectoryStream( dir );
        for (Path path : stream) {
            if(path.toFile().isDirectory()) {
                if(recursive) {
                    current = FileHelper.countFiles(path.toFile().getAbsolutePath(), true, current);
                }
            } else {
                current++;
            }
        }
        stream.close();
        return current;
    }

    public static Directory generateTree(String path, boolean recursive, ParentTask<?> task, long max) throws Exception {
        File root = new File(path);
        if(root.exists() && root.isDirectory()) {
            Directory directory = new Directory();
            directory.setId(getDir(path));
            directory.setPath(path);
            directory.setTitle(root.getName());

            File[] files = root.listFiles(ImageHelper.getFilter());
            if(files != null) {
                for(File child : files) {
                    Image image = SaveFolderTask.fileToImage(getImage(child.getAbsolutePath()), child, directory, false);
                    directory.getImages().add(image);
                    if(task != null) {
                        task.updateProgress(max, String.format(PhotoManager.GLOBALS.getLanguage().getString("msg.saveFolder.analyse"), image.getTitle()));
                    }
                }
            }

            if(recursive) {
                File[] folders = root.listFiles();
                if(folders != null) {
                    for(File folder : folders) {
                        if(folder.isDirectory()) {
                            directory.getChildren().add(generateTree(folder.getAbsolutePath(), true, task, max));
                        }
                    }
                }
            }

            return directory;
        }
        return null;
    }

    private static long getDir(String path) {
        long id = 0;
        try {
            List<Directory> directories = PhotoManager.GLOBALS.getDatabase().getDirectories("path='" + path + "'", false);
            if(directories != null) {
                if(!directories.isEmpty()) {
                    id = directories.get(0).getId();
                }
            }
        } catch (Exception ignored) {}
        return id;
    }

    private static long getImage(String path) {
        long id = 0;
        try {
            List<Image> images = PhotoManager.GLOBALS.getDatabase().getImages("path='" + path + "'");
            if(images != null) {
                if(!images.isEmpty()) {
                    id = images.get(0).getId();
                }
            }
        } catch (Exception ignored) {}
        return id;
    }
}
