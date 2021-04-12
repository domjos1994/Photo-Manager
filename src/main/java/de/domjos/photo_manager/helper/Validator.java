package de.domjos.photo_manager.helper;

import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Validator {
    private final Map<String, Boolean> states;


    public Validator() {
        this.states = new LinkedHashMap<>();
    }

    public void validateInteger(TextInputControl textInputControl, boolean mandatory, TitledPane panel) {
        this.states.put(textInputControl.getId(), this.checkInteger(textInputControl, mandatory, panel));

        textInputControl.textProperty().addListener((observable, oldValue, newValue) -> this.states.put(textInputControl.getId(), this.checkInteger(textInputControl, mandatory, panel)));
    }

    public void validateFieldsTogetherMandatory(List<TextInputControl> textInputControls, TitledPane panel) {
        for(TextInputControl txt : textInputControls) {
            this.states.put(txt.getId(), this.checkMandatoryTogether(textInputControls, panel));

            txt.textProperty().addListener((observable, oldValue, newValue) -> {
                for(TextInputControl txt2 : textInputControls) {
                    this.states.put(txt2.getId(), this.checkMandatoryTogether(textInputControls, panel));
                }
            });
        }
    }

    public boolean check() {
        boolean valid = true;

        for(boolean state : this.states.values()) {
            if(!state) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    private boolean checkMandatoryTogether(List<TextInputControl> textInputControls, TitledPane panel) {
        boolean result = true;

        boolean isNotEmpty = false;
        for(TextInputControl txt : textInputControls) {
            if(txt == null) {
                return false;
            }
            if(txt.getText() == null) {
                return false;
            }

            if(!txt.getText().isEmpty()) {
                isNotEmpty = true;
                break;
            }
        }

        if(isNotEmpty) {
            for(TextInputControl txt : textInputControls) {
                if(txt.getText().isEmpty()) {
                    result = false;
                    break;
                }
            }
        }

        for(TextInputControl textInputControl : textInputControls) {
            this.showError(textInputControl, panel, result);
        }

        return result;
    }

    private boolean checkInteger(TextInputControl textInputControl, boolean mandatory, TitledPane panel) {
        boolean result = false;
        if(textInputControl != null) {
            if(textInputControl.getText() != null) {
                if(textInputControl.getText().isEmpty()) {
                    result = !mandatory;
                } else {
                    try {
                        Integer.parseInt(textInputControl.getText());
                        result = true;
                    } catch (Exception ignored) {}
                }
            }

            this.showError(textInputControl, panel, result);
        }

        return result;
    }

    private void showError(TextInputControl textInputControl, TitledPane panel, boolean result) {
        if(!result) {
            if(!textInputControl.getStyleClass().contains("error")) {
                textInputControl.getStyleClass().add("error");
            }
            if(panel != null) {
                if(!panel.getStyleClass().contains("error")) {
                    panel.getStyleClass().add("error");
                }
            }
        } else {
            textInputControl.getStyleClass().remove("error");
            if(panel != null) {
                panel.getStyleClass().remove("error");
            }
        }
    }
}
