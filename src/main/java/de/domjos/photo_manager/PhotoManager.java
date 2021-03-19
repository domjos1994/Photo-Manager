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
        Stage stage = Dialogs.printFXML("/fxml/main.fxml", language, InitializationHelper.getHeader(), primaryStage);
        stage.show();
        this.initSizes(stage);
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

    private void initSizes(Stage stage) {
        stage.setX(PhotoManager.GLOBALS.getSetting(Globals.POSITION_WINDOW_X, stage.getX()));
        stage.setY(PhotoManager.GLOBALS.getSetting(Globals.POSITION_WINDOW_Y, stage.getY()));
        stage.setWidth(PhotoManager.GLOBALS.getSetting(Globals.POSITION_WINDOW_WIDTH, stage.getWidth()));
        stage.setHeight(PhotoManager.GLOBALS.getSetting(Globals.POSITION_WINDOW_HEIGHT, stage.getHeight()));

        stage.widthProperty().addListener(obs -> this.savePosition(stage));
        stage.heightProperty().addListener(obs -> this.savePosition(stage));
        stage.xProperty().addListener(obs -> this.savePosition(stage));
        stage.yProperty().addListener(obs -> this.savePosition(stage));
    }

    private void savePosition(Stage stage) {
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_WINDOW_X, stage.getX(), false);
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_WINDOW_Y, stage.getY(), false);
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_WINDOW_WIDTH, stage.getWidth(), false);
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_WINDOW_HEIGHT, stage.getHeight(), false);
    }

    public static void main(String[] args) {
        PhotoManager.GLOBALS.setArguments(args);
        Application.launch(args);
    }
}
