module PhotoManager {
    requires java.desktop;
    requires java.sql;
    requires java.prefs;
    requires java.base;
    requires java.xml.bind;
    requires com.sun.xml.bind;
    requires java.activation;

    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.swing;
    requires javafx.web;

    requires com.gluonhq.maps;
    requires com.gluonhq.attach.storage;
    requires com.gluonhq.attach.util;
    requires org.controlsfx.controls;
    requires sqlite.jdbc;

    requires sardine;
    requires tinify;
    requires instagram4j;

    requires org.apache.logging.log4j.core;

    requires org.apache.commons.io;
    requires org.apache.commons.imaging;

    requires org.apache.logging.log4j;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.json;
    requires commons.net;

    opens de.domjos.photo_manager.controller to javafx.controls, javafx.fxml, javafx.base;
    opens de.domjos.photo_manager.controller.subController to javafx.controls, javafx.fxml;
    opens de.domjos.photo_manager to javafx.controls, javafx.fxml;
    opens de.domjos.photo_manager.model.gallery to javafx.base;

    exports de.domjos.photo_manager.controller to javafx.controls, javafx.fxml;
    exports de.domjos.photo_manager.controller.subController to javafx.controls, javafx.fxml;
    exports de.domjos.photo_manager.database;
    exports de.domjos.photo_manager.settings;
    exports de.domjos.photo_manager.model.gallery;
    exports de.domjos.photo_manager.model.services;
    exports de.domjos.photo_manager.helper;
    exports de.domjos.photo_manager.services;
    exports de.domjos.photo_manager;
    exports de.domjos.photo_manager.images.filter;
}