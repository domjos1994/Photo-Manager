module PhotoManager {
    requires java.desktop;
    requires java.sql;
    requires java.prefs;
    requires java.base;

    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.swing;
    requires javafx.web;
    requires javafx.graphics;

    requires GMapsFX;
    requires org.controlsfx.controls;
    requires sqlite.jdbc;

    requires sardine;
    requires java.xml.bind;
    requires com.sun.xml.bind;
    requires tinify;

    requires org.apache.logging.log4j.core;

    requires org.apache.commons.io;
    requires org.apache.commons.imaging;

    requires org.apache.logging.log4j;
    requires httpclient;
    requires httpcore;


    exports de.domjos.photo_manager.controller to javafx.controls, javafx.fxml;
    opens de.domjos.photo_manager.controller to javafx.controls, javafx.fxml;
    opens de.domjos.photo_manager to javafx.controls, javafx.fxml;
    exports de.domjos.photo_manager;
}