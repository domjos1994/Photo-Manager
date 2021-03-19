package de.domjos.photo_manager.utils;

import de.domjos.photo_manager.PhotoManager;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@SuppressWarnings("unused")
public final class Dialogs {
    private final static ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

    public static Stage printFXML(String path, ResourceBundle language, String header, Stage stage) throws Exception {
        Parent root = FXMLLoader.load(PhotoManager.class.getResource(path), language);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Dialogs.class.getResource("/styles/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(header);
        stage.getIcons().add(new Image(PhotoManager.class.getResourceAsStream("/images/header.png")));

        return stage;
    }

    public static void printAlert(Alert.AlertType type, String title, String header, String content) {
        if(PhotoManager.GLOBALS.isDebugMode()) {
            Dialogs.printNotification(type, title, header);
        } else {
            Alert alert = new Alert(type);
            Dialogs.setIcon(alert);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }

    public static boolean printConfirmDialog(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type, title, ButtonType.YES, ButtonType.NO);
        Dialogs.setIcon(alert);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }

    public static void printException(Throwable ex) {
        PhotoManager.GLOBALS.getLogger().error("Unhandled " + ex.getClass().getName(), ex);
        if(!PhotoManager.GLOBALS.isDebugMode()) {
            Dialogs.printNotification(Alert.AlertType.ERROR, "Unhandled " + ex.getClass().getName(), ex.toString());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            Dialogs.setIcon(alert);
            alert.setTitle("Unhandled " + ex.getClass().getName());
            alert.setHeaderText(ex.getLocalizedMessage());
            alert.setContentText(ex.toString());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();
            Label label = new Label("The exception stacktrace was:");
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        }
    }

    public static void printNotification(Alert.AlertType alertType, String title, String message) {
        Notifications notifications = Notifications.create();
        notifications.title(title);
        notifications.text(message);
        notifications.owner(PhotoManager.GLOBALS.getStage());
        notifications.position(Pos.BASELINE_CENTER);
        switch (alertType) {
            case ERROR:
                notifications.darkStyle().showError();
                break;
            case WARNING:
                notifications.darkStyle().showWarning();
                break;
            case INFORMATION:
                notifications.darkStyle().showInformation();
                break;
            case CONFIRMATION:
                notifications.darkStyle().showConfirm();
                break;
            default:
                notifications.darkStyle().show();
        }
    }

    public static File printDirectoryChooser(String title) {
        return Dialogs.printFileChooser(title, true, true, false, new LinkedList<>()).get(0);
    }

    public static File printSingleOpenFileChooser(String title, List<String> extensions) {
        return Dialogs.printFileChooser(title, true, false, false, extensions).get(0);
    }

    public static List<File> printMultiOpenFileChooser(String title, List<String> extensions) {
        return Dialogs.printFileChooser(title, true, false, true, extensions);
    }

    public static File printSaveFileChooser(String title, List<String> extensions) {
        return Dialogs.printFileChooser(title, false, false, false, extensions).get(0);
    }

    private static List<File> printFileChooser(String title, boolean open, boolean dir, boolean multiSelect, List<String> extensions) {
        List<File> files = new LinkedList<>();
        if(dir) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(title);
            files.add(directoryChooser.showDialog(null));
            return files;
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            if(extensions!=null) {
                for(String extension : extensions) {
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extension.split(":")[0], extension.split(":")[1]));
                }
            }
            if(open) {
                if(multiSelect) {
                    files = fileChooser.showOpenMultipleDialog(null);
                } else {
                    files.add(fileChooser.showOpenDialog(null));
                }
            } else {
                files.add(fileChooser.showSaveDialog(null));
            }
        }
        return files;
    }

    public static Dialog<AssembleResult> createAssembleDialog() {
        Dialog<AssembleResult> dialog = new Dialog<>();
        Dialogs.setIcon(dialog);
        dialog.setTitle(PhotoManager.GLOBALS.getLanguage().getString("main.image.menu.together.size"));

        dialog.getDialogPane().getButtonTypes().addAll(Dialogs.loginButtonType, ButtonType.CANCEL);


        TextField width = new TextField();
        width.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("main.image.edit.width"));
        TextField height = new TextField();
        height.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("main.image.edit.height"));

        Label label = new Label();
        label.setText(PhotoManager.GLOBALS.getLanguage().getString("settings.tiny.file"));
        TextField name = new TextField();

        CheckBox chkResizeImages = new CheckBox();
        chkResizeImages.setText(PhotoManager.GLOBALS.getLanguage().getString("main.image.menu.together.resize"));
        TextField resizedHeight = new TextField();
        resizedHeight.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("main.image.edit.height"));
        resizedHeight.setDisable(true);

        CheckBox chkResizeFit = new CheckBox();
        chkResizeFit.setText(PhotoManager.GLOBALS.getLanguage().getString("main.image.menu.together.fit"));

        chkResizeImages.selectedProperty().addListener(observable -> {
            boolean selected = chkResizeImages.isSelected();

            if(selected) {
                chkResizeFit.setSelected(false);
            }
            resizedHeight.setDisable(!selected);
            chkResizeFit.setDisable(selected);
        });

        chkResizeFit.selectedProperty().addListener(observable -> {
            boolean selected = chkResizeFit.isSelected();
            if(selected) {
                resizedHeight.setText("");
                resizedHeight.setDisable(true);
            } else {
                resizedHeight.setDisable(!chkResizeImages.isSelected());
            }
            chkResizeImages.setDisable(selected);
        });

        dialog.getDialogPane().setContent(
            Dialogs.addControls(
                Arrays.asList(
                    Arrays.asList(width, height),
                    Arrays.asList(label, name),
                    Arrays.asList(chkResizeImages, resizedHeight),
                    Collections.singletonList(chkResizeFit)
                )
            )
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    AssembleResult assembleResult = new AssembleResult();
                    assembleResult.width = Integer.parseInt(width.getText());
                    assembleResult.height = Integer.parseInt(height.getText());
                    assembleResult.name = name.getText();
                    assembleResult.scaledHeight = 0;

                    if(chkResizeImages.isSelected()) {
                        if(!resizedHeight.getText().trim().isEmpty()) {
                            assembleResult.scaledHeight = Integer.parseInt(resizedHeight.getText());
                        }
                    } else {
                        if(chkResizeFit.isSelected()) {
                            assembleResult.scaledHeight = -1;
                        }
                    }
                    return assembleResult;
                } catch (Exception ignored) {}
            }
            return null;
        });
        return dialog;
    }

    public static Dialog<String> createPasswordDialog(String oldPwd) {
        Dialog<String> dialog = new Dialog<>();
        Dialogs.setIcon(dialog);
        dialog.setTitle(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.encryption"));
        dialog.getDialogPane().getButtonTypes().addAll(Dialogs.loginButtonType, ButtonType.CANCEL);

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.encryption.new"));

        PasswordField repeatPassword = new PasswordField();
        repeatPassword.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.encryption.repeat"));

        if(oldPwd.trim().isEmpty()) {
            dialog.getDialogPane().setContent(
                Dialogs.addControls(
                    Collections.singletonList(
                        Arrays.asList(newPassword, repeatPassword)
                    )
                )
            );

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    try {
                        if(newPassword.getText().trim().equals(repeatPassword.getText().trim())) {
                            return newPassword.getText().trim();
                        } else {
                            return "";
                        }
                    } catch (Exception ignored) {}
                }
                return "";
            });

        } else {
            PasswordField oldPassword = new PasswordField();
            oldPassword.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("settings.directories.encryption.old"));

            dialog.getDialogPane().setContent(
                Dialogs.addControls(
                    Arrays.asList(
                        Collections.singletonList(oldPassword),
                        Arrays.asList(newPassword, repeatPassword)
                    )
                )
            );

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    try {
                        if(newPassword.getText().trim().equals(repeatPassword.getText().trim()) && oldPassword.getText().trim().equals(oldPwd.trim())) {
                            return newPassword.getText().trim();
                        } else {
                            return "";
                        }
                    } catch (Exception ignored) {}
                }
                return "";
            });

        }
        return dialog;
    }

    private static void setIcon(Dialog<?> dialog) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Dialogs.class.getResourceAsStream("/images/header.png")));
    }

    public static Dialog<ResizeResult> createResizeDialog() {
        Dialog<ResizeResult> dialog = new Dialog<>();
        Dialogs.setIcon(dialog);
        dialog.setTitle(PhotoManager.GLOBALS.getLanguage().getString("main.image.menu.resize"));

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        TextField width = new TextField();
        width.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("main.image.edit.width"));
        TextField height = new TextField();
        height.setPromptText(PhotoManager.GLOBALS.getLanguage().getString("main.image.edit.height"));

        dialog.getDialogPane().setContent(
            Dialogs.addControls(
                Collections.singletonList(
                    Arrays.asList(width, height)
                )
            )
        );

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == loginButtonType) {
                ResizeResult resizeResult = new ResizeResult();
                resizeResult.width = -1;
                resizeResult.height = -1;
                try {
                    if(!width.getText().trim().isEmpty()) {
                        resizeResult.width = Integer.parseInt(width.getText());
                    }
                } catch (Exception ignored) {}
                try {
                    if(!height.getText().trim().isEmpty()) {
                        resizeResult.height = Integer.parseInt(height.getText());
                    }
                } catch (Exception ignored) {}
                return resizeResult;
            }
            return null;
        });

        return dialog;
    }

    private static GridPane addControls(List<List<Control>> controls) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        for(int i = 0; i<=controls.size()-1; i++) {
            for(int j = 0; j<=controls.get(i).size()-1; j++) {
                gridPane.add(controls.get(i).get(j), j, i);
            }
        }
        return gridPane;
    }

    public static class AssembleResult {
        public int width, height, scaledHeight;
        public String name;
    }

    public static class ResizeResult {
        public int width, height;
    }
}
