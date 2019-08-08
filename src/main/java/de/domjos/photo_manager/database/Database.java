package de.domjos.photo_manager.database;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.DescriptionObject;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class Database {
    private Connection connection;

    public Database(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public void executeUpdate(String query) throws Exception {
        Statement statement = this.connection.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }

    private PreparedStatement prepare(String query) throws Exception {
        return this.connection.prepareStatement(query);
    }

    public void close() throws Exception {
        this.connection.close();
    }

    public long insertOrUpdateDirectory(Directory directory, long id, boolean recursive) throws Exception {
        PreparedStatement preparedStatement = this.prepare("INSERT INTO directories(name, path, isROOT) VALUES(?, ?, ?)");
        preparedStatement.setString(1, directory.getName());
        preparedStatement.setString(2, directory.getPath());
        preparedStatement.setInt(3, 0);
        preparedStatement.executeUpdate();
        long genId = this.getGeneratedId(preparedStatement);
        preparedStatement.close();

        preparedStatement = this.prepare("INSERT INTO children(parent, child) VALUES(?, ?)");
        preparedStatement.setLong(1, id);
        preparedStatement.setLong(2, genId);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        directory.setId(genId);
        this.getImages(directory, true);

        if(recursive) {
            File file = new File(directory.getPath());
            File[] paths = file.listFiles(File::isDirectory);
            if(paths!=null) {
                for(File sub  : paths) {
                    if(sub.isDirectory() && !sub.getName().startsWith(".")) {
                        Directory subDir = new Directory();
                        subDir.setName(sub.getName());
                        subDir.setPath(sub.getAbsolutePath());
                        subDir.setRoot(false);
                        subDir.setId(this.insertOrUpdateDirectory(subDir, genId, true));
                        this.getImages(subDir, true);
                    }
                }
            }
        }
        return genId;
    }

    public void deleteDirectory(Directory directory) throws Exception {
        PreparedStatement preparedStatement = this.prepare("SELECT child FROM children WHERE parent=" + directory.getId());
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Long> children = new LinkedList<>();
        while (resultSet.next()) {
            children.add(resultSet.getLong(1));
        }
        resultSet.close();
        preparedStatement.close();

        for(long child : children) {
            this.executeUpdate("DELETE FROM images WHERE parent=" + child);
            this.executeUpdate("DELETE FROM directories WHERE id=" + child);
            Directory sub = new Directory();
            sub.setId(child);
            deleteDirectory(sub);
        }

        this.executeUpdate("DELETE FROM children WHERE parent=" + directory.getId());
        this.executeUpdate("DELETE FROM directories WHERE id=" + directory.getId());
    }

    public void addRoot() throws Exception {
        boolean exists = false;
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM directories WHERE isRoot=1");
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            exists = true;
        }
        rs.close();
        preparedStatement.close();

        if(!exists) {
            preparedStatement = this.prepare("INSERT INTO directories(name, path, isROOT) VALUES('ROOT', '', 1)");
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
    }

    public Directory getRoot() throws Exception {
        Directory directory = new Directory();
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM directories WHERE isRoot=1");
        ResultSet rs = preparedStatement.executeQuery();
        rs.next();
        directory.setId(rs.getInt("id"));
        directory.setName(rs.getString("name"));
        rs.close();
        preparedStatement.close();
        this.getChildren(directory);
        return directory;
    }

    public void insertOrUpdateImage(Image image) throws Exception {
        PreparedStatement preparedStatement;
        if(image.getId()!=0) {
            preparedStatement = this.prepare("UPDATE images SET name=?, path=?, thumbnail=?, parent=?, category=? WHERE id=?");
            preparedStatement.setLong(6, image.getId());

        } else {
            preparedStatement = this.prepare("INSERT INTO images(name, path, thumbnail, parent, category) VALUES(?, ?, ?, ?, ?)");
        }
        preparedStatement.setString(1, image.getName());
        preparedStatement.setString(2, image.getPath());
        preparedStatement.setBytes(3, image.getThumbnail());
        preparedStatement.setLong(4, image.getDirectory().getId());
        if(image.getCategory()!=null) {
            preparedStatement.setLong(5, this.insertOrUpdateDescriptionObject(image.getCategory(), 0));
        } else {
            preparedStatement.setNull(5, Types.INTEGER);
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();

        if(!image.getTags().isEmpty()) {
            long id;
            if(image.getId()==0) {
                id = getGeneratedId(preparedStatement);
            } else {
                id = image.getId();
            }
            this.executeUpdate("DELETE FROM images_tags WHERE image=" + id);
            for(DescriptionObject descriptionObject : image.getTags()) {
                long tag_id = this.insertOrUpdateDescriptionObject(descriptionObject, 1);
                this.executeUpdate(String.format("INSERT INTO images_tags(image, tag) VALUES('%s', '%s')", id, tag_id));
            }
        }
    }

    public List<Image> getImages(Directory directory, boolean fromFolder) throws Exception {
        List<Image> images = new LinkedList<>();

        if(fromFolder) {
            String path = directory.getPath();
            File[] files = new File(path).listFiles(ImageHelper.getFilter());
            if(files!=null) {
                for(File file : files) {
                    try {
                        Image image = new Image();
                        image.setDirectory(directory);
                        image.setName(file.getName());
                        image.setPath(file.getAbsolutePath());
                        BufferedImage bufferedImage = ImageHelper.scale(ImageHelper.getImage(image.getPath()), 50, 50);
                        image.setThumbnail(ImageHelper.imageToByteArray(bufferedImage));
                        this.insertOrUpdateImage(image);
                    } catch (Exception ex) {
                        PhotoManager.GLOBALS.getLogger().error("Read Image", ex);
                    }
                }
            }
            images = this.getImages(directory, false);
        } else {
            PreparedStatement preparedStatement = this.prepare("SELECT * FROM images WHERE parent=" + directory.getId());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Image image = new Image();
                image.setId(rs.getInt("id"));
                image.setName(rs.getString("name"));
                image.setPath(rs.getString("path"));
                image.setDirectory(directory);
                image.setThumbnail(rs.getBytes("thumbnail"));
                images.add(image);
            }
            rs.close();
            preparedStatement.close();
        }
        return images;
    }

    private long insertOrUpdateDescriptionObject(DescriptionObject descriptionObject, int type) throws Exception {
        String table;
        if(type==0) {
            table = "categories";
        } else {
            table = "tags";
        }

        long id = 0;
        PreparedStatement preparedStatement = this.prepare(String.format("SELECT ID FROM %s WHERE title='%s'", table, descriptionObject.getTitle()));
        ResultSet resultSet = preparedStatement.getResultSet();
        while (resultSet.next()) {
            id = resultSet.getInt(1);
        }
        resultSet.close();
        preparedStatement.close();

        if(id==0) {
            preparedStatement = this.prepare(String.format("INSERT INTO %s(title, description) VALUES(?,?)", table));
            preparedStatement.setString(1, descriptionObject.getTitle());
            preparedStatement.setString(2, descriptionObject.getDescription());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            id = this.getGeneratedId(preparedStatement);
        }
        return id;
    }

    private void getChildren(Directory directory) throws Exception {
        List<Directory> directories = new LinkedList<>();
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM directories INNER JOIN children ON children.child=directories.ID WHERE children.parent=" + directory.getId());
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            Directory sub = new Directory();
            sub.setId(rs.getInt("id"));
            sub.setName(rs.getString("name"));
            sub.setPath(rs.getString("path"));
            this.getChildren(sub);
            directories.add(sub);
        }
        rs.close();
        preparedStatement.close();
        directory.setChildren(directories);
    }

    private long getGeneratedId(PreparedStatement preparedStatement) throws Exception {
        ResultSet rs = preparedStatement.getGeneratedKeys();
        rs.next();
        long id = rs.getLong(1);
        rs.close();
        return id;
    }
}
