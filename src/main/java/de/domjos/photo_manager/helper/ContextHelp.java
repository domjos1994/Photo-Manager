package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContextHelp {
    private final Map<String, String> helpText;

    public ContextHelp() {
        this.helpText = new LinkedHashMap<>();
    }

    public void add(Control control, String text) {
        if(control != null) {
            if(control.getId() != null) {
                if (!this.helpText.containsKey(control.getId())) {
                    this.helpText.put(control.getId(), text);
                }
            }
        }
    }

    public void fireMessage(Control control) {
        if(control != null) {
            if (control.getId() != null) {
                if(this.helpText.containsKey(control.getId())) {
                    String title = PhotoManager.GLOBALS.getLanguage().getString("help.title");
                    String help = PhotoManager.GLOBALS.getHelp().getString(this.helpText.get(control.getId()));

                    Dialogs.printNotification(Alert.AlertType.INFORMATION, title, help);
                }
            }
        }
    }
}
