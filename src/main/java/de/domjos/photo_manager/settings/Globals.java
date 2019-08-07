package de.domjos.photo_manager.settings;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.database.Database;
import javafx.stage.Stage;

import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Globals {
    private boolean debugMode;
    private Stage stage;
    private Logger logger;
    private Preferences preferences;
    private ResourceBundle language;
    private Database database;

    // Keys of general settings
    public final static String PATH = "PATH";
    public final static String TINY_KEY = "TINY";
    public final static String TINY_FILE = "TINY_FILE";

    public Globals() {
        this.debugMode = false;
        this.stage = null;
        this.logger = null;
        this.preferences = Preferences.userRoot().node(PhotoManager.class.getName());
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Stage getStage() {
        return this.stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ResourceBundle getLanguage() {
        return this.language;
    }

    public void setLanguage(ResourceBundle language) {
        this.language = language;
    }

    public Database getDatabase() {
        return this.database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public boolean isEmpty(String key) {
        return this.preferences.get(key, "").equals("");
    }

    public Object getSetting(String key, Object def) {
        if(def instanceof Boolean) {
            return this.preferences.getBoolean(key, (Boolean) def);
        } else if(def instanceof Double) {
            return this.preferences.getDouble(key, (Double) def);
        } else if(def instanceof Float) {
            return this.preferences.getFloat(key, (Float) def);
        } else if(def instanceof Integer) {
            return this.preferences.getInt(key, (Integer) def);
        } else if(def instanceof Long) {
            return this.preferences.getLong(key, (Long) def);
        } if(def instanceof byte[]) {
            return this.preferences.getByteArray(key, (byte[]) def);
        } else {
            return this.preferences.get(key, (String) def);
        }
    }

    public void saveSetting(String key, Object value) {
        if(value instanceof Boolean) {
            this.preferences.putBoolean(key, (Boolean) value);
        } else if(value instanceof Double) {
            this.preferences.putDouble(key, (Double) value);
        } else if(value instanceof Float) {
            this.preferences.putFloat(key, (Float) value);
        } else if(value instanceof Integer) {
            this.preferences.putInt(key, (Integer) value);
        } else if(value instanceof Long) {
            this.preferences.putLong(key, (Long) value);
        } else if(value instanceof String) {
            this.preferences.put(key, (String) value);
        } else if(value instanceof byte[]) {
            this.preferences.putByteArray(key, (byte[]) value);
        } else {
            this.preferences.put(key, String.valueOf(value));
        }
    }
}
