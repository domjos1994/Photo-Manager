CREATE TABLE IF NOT EXISTS categories(
    ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS tags(
     ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
     title VARCHAR(255) NOT NULL,
     description TEXT
);

CREATE TABLE IF NOT EXISTS directories(
      ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
      name VARCHAR(255) NOT NULL,
      path TEXT NOT NULL,
      isRoot TINYINT NOT NULL DEFAULT 0,
      isLibrary TINYINT DEFAULT 0,
      isRecursive TINYINT DEFAULT 0,
      cloud_id INTEGER DEFAULT 0,
      folder INTEGER DEFAULT 0,
      FOREIGN KEY(cloud_id) REFERENCES cloud_inclusion(ID) ON DELETE CASCADE ON UPDATE CASCADE,
      FOREIGN KEY(folder) REFERENCES folders(ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS folders(
      ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
      icon TEXT,
      password VARCHAR(255) DEFAULT '',
      batch INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS children(
       ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
       parent INTEGER NOT NULL,
       child INTEGER NOT NULL,
       FOREIGN KEY(parent) REFERENCES directories(ID) ON DELETE CASCADE ON UPDATE CASCADE,
       FOREIGN KEY(child) REFERENCES directories(ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS images(
     ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
     parent INTEGER NOT NULL,
     name VARCHAR(255) NOT NULL,
     path TEXT NOT NULL,
     thumbnail BLOB NOT NULL,
     category INTEGER DEFAULT 0,
     width INTEGER NOT NULL,
     height INTEGER NOT NULL,
     cloud_id INTEGER DEFAULT 0,
     FOREIGN KEY(cloud_id) REFERENCES cloud_inclusion(ID) ON DELETE CASCADE ON UPDATE CASCADE,
     FOREIGN KEY(parent) REFERENCES directories(ID) ON DELETE CASCADE ON UPDATE CASCADE,
     FOREIGN KEY(category) REFERENCES categories(ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS images_tags(
    ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    image INTEGER NOT NULL,
    tag INTEGER NOT NULL,
    FOREIGN KEY(image) REFERENCES images(ID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(tag) REFERENCES tags(ID) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS images_edited(
    ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    image INTEGER NOT NULL,
    type VARCHAR(255) NOT NULL,
    value DOUBLE DEFAULT 0.0,
    stringValue VARCHAR(255) DEFAULT '',
    FOREIGN KEY(image) REFERENCES images(ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS cloud_inclusion(
    ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    path TEXT
);

CREATE TABLE IF NOT EXISTS templates(
    ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONTENT TEXT
);

CREATE TABLE IF NOT EXISTS batchTemplates(
    ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    width INTEGER DEFAULT -1,
    height INTEGER DEFAULT -1,
    compress TINYINT DEFAULT 0,
    rename VARCHAR(255) DEFAULT '',
    folder TINYINT DEFAULT 0,
    targetFolder INTEGER DEFAULT 0,
    ftp TINYINT DEFAULT 0,
    server VARCHAR(255) DEFAULT '',
    user VARCHAR(255) DEFAULT '',
    pwd VARCHAR(500) DEFAULT '',
    ftpSecure TINYINT DEFAULT 0,
    path VARCHAR(5000) DEFAULT '',
    FOREIGN KEY(targetFolder) REFERENCES directories(ID) ON DELETE CASCADE ON UPDATE CASCADE
);