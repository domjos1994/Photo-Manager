package de.domjos.photo_manager.database;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.Template;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.gallery.TemporaryEdited;
import de.domjos.photo_manager.model.services.Cloud;
import de.domjos.photo_manager.services.SaveFolderTask;

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

    public void updateDirectory(Directory directory) throws Exception {
        if(directory.getCloud()!=null) {
            directory.getCloud().setId(this.insertCloud(directory.getCloud()));
        }

        PreparedStatement preparedStatement = this.prepare("UPDATE directories SET cloud_id=? WHERE ID=?");
        preparedStatement.setLong(2, directory.getId());
        if(directory.getCloud()!=null) {
            preparedStatement.setLong(1, directory.getCloud().getId());
        } else {
            preparedStatement.setNull(1, Types.INTEGER);
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public long insertOrUpdateDirectory(Directory directory, long id, boolean recursive, SaveFolderTask task) throws Exception {
        if(directory.getCloud()!=null) {
            directory.getCloud().setId(this.insertCloud(directory.getCloud()));
        }

        PreparedStatement preparedStatement = this.prepare("INSERT INTO directories(name, path, isROOT, isLibrary, isRecursive, cloud_id) VALUES(?, ?, ?, ?, ?, ?)");
        preparedStatement.setString(1, directory.getTitle());
        preparedStatement.setString(2, directory.getPath());
        preparedStatement.setInt(3, 0);
        preparedStatement.setInt(4, directory.isLibrary() ? 1 : 0);
        preparedStatement.setInt(5, directory.isRecursive() ? 1 : 0);
        if(directory.getCloud()!=null) {
            preparedStatement.setLong(6, directory.getCloud().getId());
        } else {
            preparedStatement.setNull(6, Types.INTEGER);
        }
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
                int i = 0;
                for(File sub  : paths) {
                    if(task!=null) {
                        task.max = paths.length;
                        task.counter.set(i);
                    }
                    if(sub.isDirectory() && !sub.getName().startsWith(".")) {
                        Directory subDir = new Directory();
                        subDir.setTitle(sub.getName());
                        subDir.setPath(sub.getAbsolutePath());
                        subDir.setRoot(false);
                        subDir.setId(this.insertOrUpdateDirectory(subDir, genId, true));
                        this.getImages(subDir, true);
                    }
                    i++;
                }
            }
        }
        return genId;
    }

    private long insertOrUpdateDirectory(Directory directory, long id, boolean recursive) throws Exception {
        return this.insertOrUpdateDirectory(directory, id, recursive, null);
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
        directory.setTitle(rs.getString("name"));
        rs.close();
        preparedStatement.close();
        this.getChildren(directory);
        return directory;
    }

    public void insertOrUpdateImage(Image image) throws Exception {
        if(image.getCloud()!=null) {
            image.getCloud().setId(this.insertCloud(image.getCloud()));
        }

        PreparedStatement preparedStatement;
        if(image.getId()!=0) {
            preparedStatement = this.prepare("UPDATE images SET name=?, path=?, thumbnail=?, parent=?, category=?, width=?, height=?, cloud_id=? WHERE id=?");
            preparedStatement.setLong(9, image.getId());

        } else {
            preparedStatement = this.prepare("INSERT INTO images(name, path, thumbnail, parent, category, width, height, cloud_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
        }
        preparedStatement.setString(1, image.getTitle());
        preparedStatement.setString(2, image.getPath());
        preparedStatement.setBytes(3, image.getThumbnail());
        preparedStatement.setLong(4, image.getDirectory().getId());
        if(image.getCategory()!=null) {
            preparedStatement.setLong(5, this.insertOrUpdateDescriptionObject(image.getCategory(), 0));
        } else {
            preparedStatement.setNull(5, Types.INTEGER);
        }
        preparedStatement.setInt(6, image.getWidth());
        preparedStatement.setInt(7, image.getHeight());
        if(image.getCloud()!=null) {
            preparedStatement.setLong(8, image.getCloud().getId());
        } else {
            preparedStatement.setNull(8, Types.INTEGER);
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
                System.out.println(descriptionObject.getTitle());
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
                        image.setTitle(file.getName());
                        image.setPath(file.getAbsolutePath());
                        BufferedImage originalImage = ImageHelper.getImage(image.getPath());
                        if(originalImage!=null) {
                            image.setWidth(originalImage.getWidth());
                            image.setHeight(originalImage.getHeight());
                            BufferedImage bufferedImage;
                            if(originalImage.getWidth()>originalImage.getHeight()) {
                                double factor = originalImage.getWidth() / 200.0;
                                bufferedImage = ImageHelper.scale(originalImage, 200, (int) (originalImage.getHeight()/factor));
                            } else if(originalImage.getWidth()<originalImage.getHeight()) {
                                double factor = originalImage.getHeight() / 200.0;
                                bufferedImage = ImageHelper.scale(originalImage, (int) (originalImage.getWidth()/factor), 200);
                            } else {
                                bufferedImage = ImageHelper.scale(originalImage, 200, 200);
                            }
                            image.setThumbnail(ImageHelper.imageToByteArray(bufferedImage));
                        }
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
                image.setTitle(rs.getString("name"));
                image.setPath(rs.getString("path"));
                image.setDirectory(directory);
                image.setHeight(rs.getInt("height"));
                image.setWidth(rs.getInt("width"));
                image.setThumbnail(rs.getBytes("thumbnail"));
                image.setCloud(this.getCloud(rs.getLong("cloud_id")));
                image.setTemporaryEditedList(this.getTemporaryEdited(image.getId()));

                List<DescriptionObject> descriptionObjects = this.getDescriptionObjects(rs.getLong("category"), 0);
                if(!descriptionObjects.isEmpty()) {
                    image.setCategory(descriptionObjects.get(0));
                }
                image.setTags(this.getDescriptionObjects(image.getId(), 1));
                images.add(image);
            }
            rs.close();
            preparedStatement.close();
        }
        return images;
    }

    public void insertOrUpdateEdited(TemporaryEdited temporaryEdited, long image) throws Exception {
        PreparedStatement preparedStatement = this.prepare("INSERT INTO images_edited(image, type, value) VALUES(?, ?, ?)");
        preparedStatement.setLong(1, image);
        preparedStatement.setString(2, temporaryEdited.getChangeType().name());
        preparedStatement.setDouble(3, temporaryEdited.getValue());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public List<TemporaryEdited> getTemporaryEdited(long image) throws Exception {
        List<TemporaryEdited> temporaryEditedList = new LinkedList<>();
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM images_edited WHERE image=" + image);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            TemporaryEdited temporaryEdited = new TemporaryEdited();
            temporaryEdited.setId(resultSet.getLong("id"));
            temporaryEdited.setChangeType(TemporaryEdited.ChangeType.valueOf(resultSet.getString("type")));
            temporaryEdited.setValue(resultSet.getDouble("value"));
            temporaryEditedList.add(temporaryEdited);
        }

        return temporaryEditedList;
    }

    public void insertOrUpdateTemplate(Template template) throws Exception {
        PreparedStatement preparedStatement;
        if(template.getId()==0) {
            preparedStatement = this.prepare("INSERT INTO templates(name, content) VALUES(?,?)");
        } else {
            preparedStatement = this.prepare("UPDATE templates SET name=?, content=? WHERE id=?");
            preparedStatement.setLong(3, template.getId());
        }
        preparedStatement.setString(1, template.getTitle());
        preparedStatement.setString(2, template.getContent());
        preparedStatement.executeUpdate();
    }

    public void deleteTemplate(String title) throws Exception {
        if(title!=null) {
            PreparedStatement preparedStatement = this.prepare("DELETE FROM templates WHERE name=?");
            preparedStatement.setString(1, title);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
    }

    public List<Template> getTemplates(String where) throws Exception {
        List<Template> templates = new LinkedList<>();

        PreparedStatement preparedStatement = this.prepare("SELECT * FROM templates" + (where.isEmpty() ? "" : " WHERE " + where));
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Template template = new Template();
            template.setId(resultSet.getLong("id"));
            template.setTitle(resultSet.getString("name"));
            template.setContent(resultSet.getString("content"));
            templates.add(template);
        }
        resultSet.close();
        preparedStatement.close();

        return templates;
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

    private List<DescriptionObject> getDescriptionObjects(long id, int type) throws Exception {
        List<DescriptionObject> descriptionObjects = new LinkedList<>();
        PreparedStatement preparedStatement;
        if(type==0) {
            preparedStatement = this.prepare("SELECT * FROM categories WHERE id=" + id);
        } else {
            preparedStatement = this.prepare("SELECT * FROM tags INNER JOIN images_tags ON images_tags.tag=tags.id WHERE images_tags.image=" + id);
        }

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            DescriptionObject descriptionObject = new DescriptionObject();
            descriptionObject.setId(resultSet.getLong(1));
            descriptionObject.setTitle(resultSet.getString(2));
            descriptionObject.setDescription(resultSet.getString(3));
            descriptionObjects.add(descriptionObject);
        }
        resultSet.close();
        preparedStatement.close();

        return descriptionObjects;
    }

    private void getChildren(Directory directory) throws Exception {
        List<Directory> directories = new LinkedList<>();
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM directories INNER JOIN children ON children.child=directories.ID WHERE children.parent=" + directory.getId());
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            Directory sub = new Directory();
            sub.setId(rs.getInt("id"));
            sub.setTitle(rs.getString("name"));
            sub.setPath(rs.getString("path"));
            sub.setLibrary(rs.getInt("isLibrary")==1);
            sub.setRecursive(rs.getInt("isRecursive")==1);
            sub.setCloud(this.getCloud(rs.getLong("cloud_id")));
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

    private Cloud getCloud(long id) throws Exception {
        Cloud cloud = null;
        PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM cloud_inclusion WHERE ID=" + id);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            cloud = new Cloud();
            cloud.setId(id);
            cloud.setPath(resultSet.getString("path"));
        }
        resultSet.close();
        preparedStatement.close();
        return cloud;
    }

    private long insertCloud(Cloud cloud) throws Exception {
        PreparedStatement preparedStatement;
        if(cloud.getId()==0) {
            preparedStatement = this.connection.prepareStatement("INSERT INTO cloud_inclusion(path) VALUES(?)");

        } else {
            preparedStatement = this.connection.prepareStatement("UPDATE cloud_inclusion SET path=? WHERE id=?");
            preparedStatement.setLong(2, cloud.getId());
        }

        preparedStatement.setString(1, cloud.getPath());
        preparedStatement.executeUpdate();
        if(cloud.getId()==0) {
            cloud.setId(this.getGeneratedId(preparedStatement));
        }
        return cloud.getId();
    }
}
