package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.database.Database;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The class which initializes the database
 * @author Dominic Joas
 * @version 0.1
 */
public class InitializationHelper {
    public final static String HIDDEN_PROJECT_DIR = ".photoManager";

    /**
     * Opens a Dialog if saved path doesn't exists
     * and creates the path.
     */
    public static void initializePath(String path) {
        if(path.isEmpty()) {
            if(PhotoManager.GLOBALS.isEmpty(Globals.PATH)) {
                File files = Dialogs.printDirectoryChooser(PhotoManager.GLOBALS.getLanguage().getString("main.initialize.path"));
                if(files!=null) {
                    PhotoManager.GLOBALS.saveSetting(Globals.PATH, files.getAbsolutePath(), false);
                    File file = new File(PhotoManager.GLOBALS.getSetting(Globals.PATH, "") + File.separatorChar + InitializationHelper.HIDDEN_PROJECT_DIR);
                    if(!file.exists()) {
                        if(!file.mkdirs()) {
                            Platform.exit();
                        }
                    }
                } else {
                    Platform.exit();
                }
            } else {
                File file = new File(PhotoManager.GLOBALS.getSetting(Globals.PATH, "") + File.separatorChar + InitializationHelper.HIDDEN_PROJECT_DIR);
                if(!file.exists()) {
                    PhotoManager.GLOBALS.saveSetting(Globals.PATH, "", false);
                    InitializationHelper.initializePath(path);
                }
            }
        } else {
            File file = new File(PhotoManager.GLOBALS.getSetting(Globals.PATH, "") + File.separatorChar + InitializationHelper.HIDDEN_PROJECT_DIR);
            if(!file.exists()) {
                PhotoManager.GLOBALS.saveSetting(Globals.PATH, "", false);
                InitializationHelper.initializePath(path);
            } else {
                PhotoManager.GLOBALS.saveSetting(Globals.PATH, path, false);
                InitializationHelper.initializePath(path);
            }
        }
    }

    /**
     * Initialize Log4J
     * Copies the log4j2.xml from resources to selected path
     * @return the logger object
     * @throws Exception exception
     */
    public static Logger initializeLogger() throws Exception {
        // copy config file to application-path if not exists
        String path = PhotoManager.GLOBALS.getSetting(Globals.PATH, "") + File.separatorChar + InitializationHelper.HIDDEN_PROJECT_DIR + File.separatorChar;
        File logConfig = new File(path + "log4j2.xml");
        if(!logConfig.exists()) {
            FileUtils.copyInputStreamToFile(PhotoManager.class.getResourceAsStream("/properties/log4j2.xml"), logConfig);
        }

        // set system-properties for log4j
        System.setProperty("filename", path + "error.log");
        System.setProperty("log4j.configurationFile", path + "log4j2.xml");

        // init logger and add it to globals
        return LogManager.getLogger();
    }

    /**
     * Creates Database or uses the available
     * @return the database-class
     * @throws Exception exception
     */
    public static Database initializeDatabase() throws Exception {
        Database database = new Database(PhotoManager.GLOBALS.getSetting(Globals.PATH, "") + File.separatorChar + InitializationHelper.HIDDEN_PROJECT_DIR + File.separatorChar + "photoManager.db");
        String initFile = Helper.readResource("/sql/init.sql");
        for(String query : initFile.split(";")) {
            database.executeUpdate(query.trim());
        }

        String updateFile = Helper.readResource("/sql/update.sql");
        for(String query : updateFile.split(";")) {
            database.executeUpdate(query.trim());
        }

        database.addRoot();
        return database;
    }

    /**
     * Chooses the current language
     * @return the current resource-bundle
     */
    public static ResourceBundle getResourceBundle() {
        URL[] urls = new URL[]{InitializationHelper.class.getResource("/languages/")};
        URLClassLoader classLoader = new URLClassLoader(urls);
        if(Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage())) {
            return ResourceBundle.getBundle("lang", Locale.GERMAN, classLoader);
        } else {
            return ResourceBundle.getBundle("lang", Locale.ENGLISH, classLoader);
        }
    }

    /**
     * Prints the header
     * app-name + version
     * @return the header
     */
    public static String getHeader() {
        String title = "PhotoManager";
        if(PhotoManager.GLOBALS.isDebugMode()) {
            title += " - (Debug)";
        }
        title += " - " + PhotoManager.GLOBALS.getSetting(Globals.PATH, "");
        return title;
    }
}
