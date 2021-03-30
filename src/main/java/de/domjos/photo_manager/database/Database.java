package de.domjos.photo_manager.database;

import de.domjos.photo_manager.model.gallery.*;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.model.services.Cloud;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Database {
    private final Connection connection;

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

    public long insertOrUpdateDirectory(Directory directory, long parent, boolean recursive) throws Exception {
        PreparedStatement preparedStatement;
        if(directory.getId() == 0) {
            preparedStatement = this.prepare("INSERT INTO directories(name, path, isRoot, isLibrary, isRecursive, cloud_id, folder) VALUES(?,?,?,?,?,?,?)");
        } else {
            preparedStatement = this.prepare("UPDATE directories SET name=?, path=?, isRoot=?, isLibrary=?, isRecursive=?, cloud_id=?, folder=? WHERE id=?");
            preparedStatement.setLong(8, directory.getId());
        }
        preparedStatement.setString(1, directory.getTitle());
        preparedStatement.setString(2, directory.getPath());
        preparedStatement.setBoolean(3, directory.isRoot());
        preparedStatement.setBoolean(4, directory.isLibrary());
        preparedStatement.setBoolean(5, directory.isRecursive());

        if(directory.getCloud() != null) {
            preparedStatement.setLong(6, this.insertCloud(directory.getCloud()));
        } else {
            preparedStatement.setNull(6, Types.INTEGER);
        }
        if(directory.getFolder() != null) {
            preparedStatement.setLong(7, this.insertOrUpdateFolder(directory.getFolder()));
        } else {
            preparedStatement.setNull(7, Types.INTEGER);
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();

        long id;
        if(directory.getId() == 0) {
            id = getGeneratedId(preparedStatement);
        } else {
            id = directory.getId();
        }

        if(recursive) {
            preparedStatement = this.prepare("DELETE FROM children WHERE parent=" + id);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            for(Directory child : directory.getChildren()) {
                preparedStatement = this.prepare("INSERT INTO children(parent, child) VALUES(?, ?)");
                preparedStatement.setLong(1, id);
                preparedStatement.setLong(2, this.insertOrUpdateDirectory(child, id, true));
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } else {
            if(parent != -1) {
                if(parent != id) {
                    preparedStatement = this.prepare("DELETE FROM children WHERE parent=" + parent + " AND child=" + id);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();

                    preparedStatement = this.prepare("INSERT INTO children(parent, child) VALUES(?, ?)");
                    preparedStatement.setLong(1, parent);
                    preparedStatement.setLong(2, id);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
            }
        }

        return id;
    }

    public List<Directory> getDirectories(String where, boolean recursive) throws Exception {
        Map<Directory, String> dirsAndChildren = new LinkedHashMap<>();

        where = where.trim();
        if(!where.isEmpty()) {
            where = " WHERE " + where;
        }

        PreparedStatement preparedStatement = this.prepare("SELECT * FROM directories" + where);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Directory directory = new Directory();
            directory.setId(resultSet.getLong("id"));
            directory.setTitle(resultSet.getString("name"));
            directory.setPath(resultSet.getString("path"));
            directory.setRoot(resultSet.getBoolean("isRoot"));
            directory.setLibrary(resultSet.getBoolean("isLibrary"));
            directory.setRecursive(resultSet.getBoolean("isRecursive"));

            long cloud_id = resultSet.getLong("cloud_id");
            if(cloud_id != 0) {
                directory.setCloud(this.getCloud(cloud_id));
            }

            long folder = resultSet.getLong("folder");
            if(folder != 0) {
                directory.setFolder(this.getFolder(folder, directory.getId()));
            }

            dirsAndChildren.put(directory, this.getChildren(directory.getId()));
        }
        resultSet.close();
        preparedStatement.close();

        List<Directory> directories = new LinkedList<>();
        for(Map.Entry<Directory, String> entry : dirsAndChildren.entrySet()) {
            Directory directory = entry.getKey();
            if(recursive) {
                directory.setChildren(this.getDirectories("id IN " + entry.getValue(), true));
            }
            directories.add(directory);
        }
        return directories;
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
        if(directory.getCloud() != null) {
            this.executeUpdate("DELETE FROM cloud WHERE id=" + directory.getCloud().getId());
        }
    }

    public List<Image> getImages(String where) throws Exception {
        List<Image> images = new LinkedList<>();

        where = where.trim();
        if(!where.isEmpty()) {
            where = " WHERE " + where;
        }

        PreparedStatement preparedStatement = this.prepare("SELECT * FROM images" + where);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Image image = new Image();
            image.setId(resultSet.getLong("id"));

            try {
                long parent = resultSet.getLong("parent");
                if(parent != 0) {
                    image.setDirectory(this.getDirectories("id=" + parent, false).get(0));
                }
            } catch (Exception ignored) {}

            image.setTitle(resultSet.getString("name"));
            image.setPath(resultSet.getString("path"));
            image.setThumbnail(resultSet.getBytes("thumbnail"));

            try {
                long category = resultSet.getLong("category");
                if(category != 0) {
                    image.setCategory(this.getDescriptionObjects(category, 0).get(0));
                }
            } catch (Exception ignored) {}

            image.setWidth(resultSet.getInt("width"));
            image.setHeight(resultSet.getInt("height"));

            try {
                long cloudId = resultSet.getLong("cloud_id");
                if(cloudId != 0) {
                    image.setCloud(this.getCloud(cloudId));
                }
            } catch (Exception ignored) {}

            try {
                image.setTags(this.getDescriptionObjects(image.getId(), 1));
            } catch (Exception ignored) {}

            images.add(image);
        }
        resultSet.close();
        preparedStatement.close();

        return images;
    }

    public void deleteImage(Image image) throws Exception {
        this.executeUpdate("DELETE FROM images WHERE ID=" + image.getId());
        this.executeUpdate("DELETE FROM images_tags WHERE image=" + image.getId());
        this.executeUpdate("DELETE FROM images_edited WHERE image=" + image.getId());
    }

    public void deleteEdited(TemporaryEdited temporaryEdited) throws Exception {
        this.executeUpdate("DELETE FROM images_edited WHERE ID=" + temporaryEdited.getId());
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

    public void insertOrUpdateEdited(TemporaryEdited temporaryEdited, long image) throws Exception {
        PreparedStatement preparedStatement = this.prepare("INSERT INTO images_edited(image, type, value, stringValue) VALUES(?, ?, ?, ?)");
        preparedStatement.setLong(1, image);
        preparedStatement.setString(2, temporaryEdited.getChangeType().name());
        preparedStatement.setDouble(3, temporaryEdited.getValue());
        preparedStatement.setString(4, temporaryEdited.getStringValue());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public List<TemporaryEdited> getTemporaryEdited(long image) throws Exception {
        List<TemporaryEdited> temporaryEditedList = new LinkedList<>();
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM images_edited WHERE image=" + image);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            TemporaryEdited.ChangeType changeType = TemporaryEdited.ChangeType.valueOf(resultSet.getString("type"));

            TemporaryEdited temporaryEdited = new TemporaryEdited();
            temporaryEdited.setId(resultSet.getLong("id"));
            temporaryEdited.setChangeType(changeType);
            if(changeType != TemporaryEdited.ChangeType.Watermark && changeType != TemporaryEdited.ChangeType.Resize && changeType != TemporaryEdited.ChangeType.Filter) {
                temporaryEdited.setValue(resultSet.getDouble("value"));
            } else {
                temporaryEdited.setStringValue(resultSet.getString("stringValue"));
            }
            temporaryEditedList.add(temporaryEdited);
        }

        return temporaryEditedList;
    }

    public void removeHistory(TemporaryEdited temporaryEdited, long image) throws Exception {
        PreparedStatement preparedStatement = this.prepare("DELETE FROM images_edited WHERE image=? AND id=?");
        preparedStatement.setLong(1, image);
        preparedStatement.setLong(2, temporaryEdited.getId());
        preparedStatement.executeUpdate();
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

    public void insertOrUpdateBatchTemplate(BatchTemplate batchTemplate) throws Exception {
        PreparedStatement preparedStatement;
        if(batchTemplate.getId() == 0) {
            preparedStatement = this.prepare("INSERT INTO batchTemplates(name, width, height, compress, rename, folder, targetFolder, dirRow, ftp, server, user, pwd, ftpSecure, path) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
        } else {
            preparedStatement = this.prepare("UPDATE batchTemplates SET name=?, width=?, height=?, compress=?, rename=?, folder=?, targetFolder=?, dirRow=?, ftp=?, server=?, user=?, pwd=?, ftpSecure=?, path=? WHERE id=?");
            preparedStatement.setLong(15, batchTemplate.getId());
        }
        preparedStatement.setString(1, batchTemplate.getTitle());
        preparedStatement.setInt(2, batchTemplate.getWidth());
        preparedStatement.setInt(3, batchTemplate.getHeight());
        preparedStatement.setInt(4, this.boolToInt(batchTemplate.isCompress()));
        preparedStatement.setString(5, batchTemplate.getRename());
        preparedStatement.setInt(6, this.boolToInt(batchTemplate.isFolder()));
        if(batchTemplate.getTargetFolder() != null) {
            preparedStatement.setLong(7, batchTemplate.getTargetFolder().getId());
        } else {
            preparedStatement.setLong(7, 0);
        }
        preparedStatement.setInt(9, this.boolToInt(batchTemplate.isFtp()));
        preparedStatement.setString(10, batchTemplate.getServer());
        preparedStatement.setString(11, batchTemplate.getUser());
        preparedStatement.setString(12, batchTemplate.getPassword());
        preparedStatement.setInt(13, this.boolToInt(batchTemplate.isFtpSecure()));
        if(batchTemplate.getTargetFolderFtp() != null) {
            preparedStatement.setString(14, batchTemplate.getTargetFolderFtp().getPath());
        } else {
            preparedStatement.setString(14, "");
        }
        preparedStatement.executeUpdate();
    }

    public List<BatchTemplate> getBatchTemplates(String where) throws Exception {
        List<BatchTemplate> batchTemplates = new LinkedList<>();

        PreparedStatement preparedStatement = this.prepare("SELECT * FROM batchTemplates" + (where.isEmpty() ? "" : " WHERE " + where));
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            BatchTemplate batchTemplate = new BatchTemplate();
            batchTemplate.setId(resultSet.getLong("id"));
            batchTemplate.setTitle(resultSet.getString("name"));
            batchTemplate.setWidth(resultSet.getInt("width"));
            batchTemplate.setHeight(resultSet.getInt("height"));
            batchTemplate.setCompress(resultSet.getBoolean("compress"));
            batchTemplate.setRename(resultSet.getString("rename"));
            batchTemplate.setFolder(resultSet.getBoolean("folder"));
            int targetFolder = resultSet.getInt("targetFolder");
            if(targetFolder != 0) {
                this.getDir(this.getDirectories("isRoot=1", true).get(0), targetFolder, batchTemplate);
            } else {
                batchTemplate.setTargetFolder(null);
            }
            batchTemplate.setFtp(resultSet.getBoolean("ftp"));
            batchTemplate.setServer(resultSet.getString("server"));
            batchTemplate.setUser(resultSet.getString("user"));
            batchTemplate.setPassword(resultSet.getString("pwd"));
            batchTemplate.setFtpSecure(resultSet.getBoolean("ftpSecure"));
            Directory directory = new Directory();
            directory.setPath(resultSet.getString("path"));
            batchTemplate.setTargetFolderFtp(directory);

            batchTemplates.add(batchTemplate);
        }
        resultSet.close();
        preparedStatement.close();

        return batchTemplates;
    }

    public void deleteBatchTemplate(BatchTemplate batchTemplate) throws Exception {
        PreparedStatement preparedStatement = this.prepare("DELETE FROM batchTemplates WHERE id=?");
        preparedStatement.setLong(1, batchTemplate.getId());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public boolean columnExists(String table, String column) {
        try {
            PreparedStatement preparedStatement = this.prepare(String.format("SELECT %s FROM %s", column, table));
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.close();
            preparedStatement.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Folder getFolder(long id, long dir) throws Exception {
        Folder folder = null;
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM folders WHERE id=" + id);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            folder = new Folder();
            folder.setIcon(resultSet.getString("icon"));

            long batch_id = resultSet.getLong("batch");
            if(batch_id != 0) {
                List<BatchTemplate> batchTemplates = this.getBatchTemplates("id=" + batch_id);
                if(batchTemplates != null) {
                    if(!batchTemplates.isEmpty()) {
                        folder.setBatchTemplate(batchTemplates.get(0));
                    }
                }
            }
        }
        resultSet.close();
        preparedStatement.close();

        if(folder != null) {
            preparedStatement = this.prepare("SELECT parent FROM children WHERE child=" + dir);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                folder.setDirectory(this.getDirectories("id=" + resultSet.getLong("parent"), false).get(0));
            }
            resultSet.close();
            preparedStatement.close();
        }

        return folder;
    }

    private long insertOrUpdateFolder(Folder folder) throws Exception {
        PreparedStatement preparedStatement;

        if(folder.getId() == 0) {
            preparedStatement = this.prepare("INSERT INTO folders(icon, password, batch) VALUES(?, ?, ?)");
        } else {
            preparedStatement = this.prepare("UPDATE folders SET icon=?, password=?, batch=? WHERE id=?");
            preparedStatement.setLong(4, folder.getId());
        }
        preparedStatement.setString(1, folder.getIcon());
        preparedStatement.setString(2, "");
        if(folder.getBatchTemplate() != null) {
            preparedStatement.setLong(3, folder.getBatchTemplate().getId());
        } else {
            preparedStatement.setNull(3, Types.INTEGER);
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();

        if(folder.getId() != 0) {
            return folder.getId();
        } else {
            return this.getGeneratedId(preparedStatement);
        }
    }

    private void getDir(Directory root, int id, BatchTemplate batchTemplate) {
        if(root.getId() == id) {
            batchTemplate.setTargetFolder(root);
        }
        for(Directory directory : root.getChildren()) {
            this.getDir(directory, id, batchTemplate);
        }
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

    private long getGeneratedId(PreparedStatement preparedStatement) throws Exception {
        ResultSet rs = preparedStatement.getGeneratedKeys();
        rs.next();
        long id = rs.getLong(1);
        rs.close();
        return id;
    }

    private Cloud getCloud(long id) throws Exception {
        Cloud cloud = null;
        PreparedStatement preparedStatement = this.prepare("SELECT * FROM cloud_inclusion WHERE ID=" + id);
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
            preparedStatement = this.prepare("INSERT INTO cloud_inclusion(path) VALUES(?)");

        } else {
            preparedStatement = this.prepare("UPDATE cloud_inclusion SET path=? WHERE id=?");
            preparedStatement.setLong(2, cloud.getId());
        }

        preparedStatement.setString(1, cloud.getPath());
        preparedStatement.executeUpdate();
        if(cloud.getId()==0) {
            cloud.setId(this.getGeneratedId(preparedStatement));
        }
        return cloud.getId();
    }

    private String getChildren(long parent) throws Exception {
        StringBuilder ids = new StringBuilder("(");
        PreparedStatement preparedStatement = this.prepare("SELECT child FROM children WHERE parent=?");
        preparedStatement.setLong(1, parent);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            ids.append(resultSet.getLong("child")).append(", ");
        }
        resultSet.close();
        preparedStatement.close();
        return (ids.toString() + ")").replace(", )", ")");
    }

    private int boolToInt(boolean value) {
        if(value) {
            return 1;
        } else {
            return 0;
        }
    }
}
