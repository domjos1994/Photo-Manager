package de.domjos.photo_manager.model.gallery;

import de.domjos.photo_manager.model.objects.BaseObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class Template extends BaseObject {
    private String content;
    private Map<String, String> preferences;

    public Template() {
        super();

        this.content = "";
        this.preferences = new LinkedHashMap<>();
    }

    public String getContent() {
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, String> entry : this.preferences.entrySet()) {
            stringBuilder.append(String.format("%s:%s,", entry.getKey(), entry.getValue()));
        }
        this.content = stringBuilder.toString();
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getPreferences() {
        if(!this.content.isEmpty()) {
            this.preferences.clear();
            for(String pref : this.content.split(",")) {
                if(!pref.isEmpty()) {
                    if(pref.contains(":")) {
                        this.preferences.put(pref.split(":")[0].trim(), pref.split(":")[1].trim());
                    }
                }
            }
        }
        return this.preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    public enum Preference {
        ZOOM,
        HUE,
        BRIGHTNESS,
        SATURATION,
        Rotation,
        Watermark,
        Resize
    }

    @Override
    public String toString() {
        return super.getTitle();
    }
}
