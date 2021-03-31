package de.domjos.photo_manager.helper;

import javafx.scene.control.TextInputControl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Validator {
    private final Map<String, Boolean> states;


    public Validator() {
        this.states = new LinkedHashMap<>();
    }

    public void validateInteger(TextInputControl txt, boolean mandatory) {
        this.states.put(txt.getId(), this.checkInteger(txt, mandatory));

        txt.textProperty().addListener((observable, oldValue, newValue) -> this.states.put(txt.getId(), this.checkInteger(txt, mandatory)));
    }

    public void validateFieldsTogetherMandatory(List<TextInputControl> txts) {
        for(TextInputControl txt : txts) {
            this.states.put(txt.getId(), this.checkMandatoryTogether(txts));

            txt.textProperty().addListener((observable, oldValue, newValue) -> {
                for(TextInputControl txt2 : txts) {
                    this.states.put(txt2.getId(), this.checkMandatoryTogether(txts));
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

    private boolean checkMandatoryTogether(List<TextInputControl> txts) {
        boolean result = true;

        boolean isNotEmpty = false;
        for(TextInputControl txt : txts) {
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
            for(TextInputControl txt : txts) {
                if(txt.getText().isEmpty()) {
                    result = false;
                    break;
                }
            }
        }

        for(TextInputControl txt : txts) {
            if(!result) {
                if(!txt.getStyleClass().contains("error")) {
                    txt.getStyleClass().add("error");
                }
            } else {
                txt.getStyleClass().remove("error");
            }
        }

        return result;
    }

    private boolean checkInteger(TextInputControl txt, boolean mandatory) {
        boolean result = false;
        if(txt != null) {
            if(txt.getText() != null) {
                if(txt.getText().isEmpty()) {
                    result = !mandatory;
                } else {
                    try {
                        Integer.parseInt(txt.getText());
                        result = true;
                    } catch (Exception ignored) {}
                }
            }

            if(!result) {
                if(!txt.getStyleClass().contains("error")) {
                    txt.getStyleClass().add("error");
                }
            } else {
                txt.getStyleClass().remove("error");
            }
        }

        return result;
    }
}
