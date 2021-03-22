package de.domjos.photo_manager;

import de.domjos.photo_manager.helper.InitializationHelper;
import de.domjos.photo_manager.settings.Globals;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * To fix error add line in intellij test-configuration
 * <code>--add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED</code>
 */

@ExtendWith(ApplicationExtension.class)
class PhotoManagerTest {
    private static Preferences preferences;
    private static Path temporaryDirectory;
    private static Path tempProjectDirectory;
    private static String oldPath;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Start
    private void start(Stage stage) {
        try {
            PhotoManagerTest.tempProjectDirectory = new File(temporaryDirectory.toFile().getAbsolutePath() + File.separatorChar + InitializationHelper.HIDDEN_PROJECT_DIR).toPath();
            PhotoManagerTest.tempProjectDirectory.toFile().mkdirs();
            PhotoManagerTest.preferences = PhotoManagerTest.getSettings();
            PhotoManagerTest.oldPath = PhotoManagerTest.preferences.get(Globals.PATH, "");
            PhotoManagerTest.preferences.put(Globals.PATH, PhotoManagerTest.temporaryDirectory.toFile().getAbsolutePath());

            PhotoManager photoManager = new PhotoManager();
            photoManager.start(stage);
        } catch (Exception ignored) {}
    }

    @BeforeAll
    public static void copyFiles() {
        try {
            PhotoManagerTest.temporaryDirectory = Files.createTempDirectory("test");
            String resources = "/images/";
            for(int i = 1; i<=5; i++) {
                String image = "image_" + i + ".jpg";
                InputStream inputStream = PhotoManagerTest.class.getResourceAsStream(resources + image);
                FileOutputStream fileOutputStream = new FileOutputStream(PhotoManagerTest.temporaryDirectory.toFile().getAbsolutePath() + File.separatorChar + image);
                IOUtils.copy(inputStream, fileOutputStream);
                fileOutputStream.close();
                inputStream.close();
            }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterAll
    public static void removeFiles() {
        try {
            FileUtils.cleanDirectory(PhotoManagerTest.temporaryDirectory.toFile());
            PhotoManagerTest.temporaryDirectory.toFile().delete();

            PhotoManagerTest.preferences.put(Globals.PATH, PhotoManagerTest.oldPath);
        } catch (Exception ignored) {}
    }

    @Test
    public void testResizing(FxRobot robot) {
        SplitPane splitPane = robot.lookup("#splPaneDirectories").queryAs(SplitPane.class);
        double position = PhotoManagerTest.preferences.getDouble(Globals.POSITION_DIRECTORIES, splitPane.getDividerPositions()[0]);
        assertEquals(splitPane.getDividerPositions()[0], position, 0.001);

        splitPane = robot.lookup("#splPaneImages").queryAs(SplitPane.class);
        position = PhotoManagerTest.preferences.getDouble(Globals.POSITION_IMAGES, splitPane.getDividerPositions()[0]);
        assertEquals(splitPane.getDividerPositions()[0], position, 0.001);

        splitPane = robot.lookup("#splPaneImage").queryAs(SplitPane.class);
        position = PhotoManagerTest.preferences.getDouble(Globals.POSITION_IMAGE, splitPane.getDividerPositions()[0]);
        assertEquals(splitPane.getDividerPositions()[0], position, 0.001);
    }

    @Test
    public void importTest(FxRobot robot) {
        Button button = robot.lookup("#cmdMainAddFolder").queryButton();
        button.fire();

        TextInputControl textInputControl = robot.lookup("#txtMainFolderName").queryTextInputControl();
        textInputControl.appendText("Gallery");

        CheckBox chkRecursive = robot.lookup("#chkMainRecursive").queryAs(CheckBox.class);
        chkRecursive.setSelected(true);

        textInputControl = robot.lookup("#tmpPath").queryTextInputControl();
        textInputControl.setText(PhotoManagerTest.temporaryDirectory.toFile().getAbsolutePath());

        button = robot.lookup("#cmdMainFolderSave").queryButton();
        Button finalButton = button;
        Platform.runLater(finalButton::fire);
    }

    private static Preferences getSettings() {
        return Preferences.userRoot().node(PhotoManager.class.getName());
    }
}
