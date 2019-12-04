package de.domjos.photo_manager;

import de.domjos.photo_manager.helper.InitializationHelper;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class PhotoManager extends Application {
    public static final Globals GLOBALS = new Globals();

    static {
        System.setProperty("javafx.platform" , "Desktop");
        System.setProperty("http.agent", "Mozilla/5.0");
        System.setProperty("nashorn.args", "--no-deprecation-warning");
    }

    public void start(Stage primaryStage) throws Exception {
        // initialize basic properties
        PhotoManager.GLOBALS.setDebugMode(PhotoManager.GLOBALS.getSetting(Globals.DEBUG, false));
        PhotoManager.GLOBALS.setStage(primaryStage);

        // initialize language
        ResourceBundle language = InitializationHelper.getResourceBundle();
        PhotoManager.GLOBALS.setLanguage(language);

        // initialize path
        InitializationHelper.initializePath();

        // initialize logger
        PhotoManager.GLOBALS.setLogger(InitializationHelper.initializeLogger());

        // initialize database
        PhotoManager.GLOBALS.setDatabase(InitializationHelper.initializeDatabase());

        // initialize application-dialog
        String title = InitializationHelper.getHeader();
        if(PhotoManager.GLOBALS.isDebugMode()) {
            title += " - (Debug)";
        }
        Dialogs.printFXML("/fxml/main.fxml", language, title, false);
        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(event -> {
            try {
                for(Runnable runnable : PhotoManager.GLOBALS.getCloseRunnable()) {
                    if(runnable!=null) {
                        runnable.run();
                    }
                }
                if(PhotoManager.GLOBALS.isClose()) {
                    PhotoManager.GLOBALS.getDatabase().close();
                    Platform.exit();
                    System.exit(0);
                } else {
                    event.consume();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
