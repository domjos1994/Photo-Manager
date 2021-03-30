package de.domjos.photo_manager.settings;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.database.Database;
import de.domjos.photo_manager.utils.CryptoUtils;
import javafx.stage.Stage;

import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@SuppressWarnings("unused")
public class Globals {
    private boolean debugMode;
    private Stage stage;
    private Logger logger;
    private final Preferences preferences;
    private ResourceBundle language;
    private Database database;
    private final List<Runnable> closeRunnable;
    private boolean close;
    private String[] args;

    // Keys of general settings
    public final static String PATH = "PATH";
    public final static String OLD_PATHS = "OLD_PATHS";
    public final static String DEBUG = "DEBUG";
    public final static String TITLE_PATH = "TITLE_PATH";
    public final static String RELOAD_ON_START = "RELOAD_ON_START";

    public final static String TINY_KEY = "TINY";
    public final static String TINY_FILE = "TINY_FILE";

    public final static String CLOUD_PATH = "CLOUD_PATH";
    public final static String CLOUD_USER = "CLOUD_USER";
    public final static String CLOUD_PWD = "CLOUD_PWD";

    public final static String INSTAGRAM_USER = "INSTAGRAM_USER";
    public final static String INSTAGRAM_PWD = "INSTAGRAM_PWD";

    public final static String UNSPLASH_KEY = "UNSPLASH_KEY";
    public final static String UNSPLASH_SECRET_KEY = "UNSPLASH_SECRET_KEY";

    public final static String DIRECTORIES_DELETE_KEY = "DIRECTORIES_DELETE_KEY";
    public final static String DIRECTORIES = "DIRECTORIES_TITLE";

    // Keys of Split-Pane-Positions
    public final static String POSITION_DIRECTORIES = "POSITION_DIRECTORIES";
    public final static String POSITION_IMAGES = "POSITION_IMAGES";
    public final static String POSITION_IMAGE = "POSITION_IMAGE";
    public final static String POSITION_WINDOW_X = "POSITION_WINDOW_X";
    public final static String POSITION_WINDOW_Y = "POSITION_WINDOW_Y";
    public final static String POSITION_WINDOW_WIDTH = "POSITION_WINDOW_WIDTH";
    public final static String POSITION_WINDOW_HEIGHT = "POSITION_WINDOW_HEIGHT";

    public Globals() {
        this.debugMode = false;
        this.stage = null;
        this.logger = null;
        this.preferences = Preferences.userRoot().node(PhotoManager.class.getName());
        this.closeRunnable = new LinkedList<>();
        this.close = false;
        this.args = null;
    }

    public String[] getArguments() {
       return this.args;
    }

    public void setArguments(String[] args) {
        this.args = args;
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

    public boolean getSetting(String key, boolean def) {
        return this.preferences.getBoolean(key, def);
    }

    public double getSetting(String key, double def) {
        return this.preferences.getDouble(key, def);
    }

    public float getSetting(String key, float def) {
        return this.preferences.getFloat(key, def);
    }

    public int getSetting(String key, int def) {
        return this.preferences.getInt(key, def);
    }

    public long getSetting(String key, long def) {
        return this.preferences.getLong(key, def);
    }

    public byte[] getSetting(String key, byte[] def) {
        return this.preferences.getByteArray(key, def);
    }

    public String getSetting(String key, String def) {
        return this.preferences.get(key, def);
    }

    public String getDecryptedSetting(String key, String def) {
        if(!this.isEmpty(key)) {
            return CryptoUtils.decrypt(this.preferences.get(key, def));
        } else {
            return "";
        }
    }

    public void saveSetting(String key, Object value, boolean enc) {
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
            if(enc) {
                this.preferences.put(key, CryptoUtils.encrypt((String) value));
            } else {
                this.preferences.put(key, (String) value);
            }
        } else if(value instanceof byte[]) {
            this.preferences.putByteArray(key, (byte[]) value);
        } else {
            this.preferences.put(key, String.valueOf(value));
        }
    }

    public void deleteSetting(String key) {
        this.preferences.remove(key);
    }

    public List<Runnable> getCloseRunnable() {
        return this.closeRunnable;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }
}
