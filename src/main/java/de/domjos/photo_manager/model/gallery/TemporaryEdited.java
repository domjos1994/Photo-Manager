package de.domjos.photo_manager.model.gallery;

public class TemporaryEdited {
    private long id;
    private ChangeType changeType;
    private double value;
    private String stringValue;

    public TemporaryEdited() {
        this.id = 0;
        this.changeType = null;
        this.value = 0.0;
        this.stringValue = "";
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ChangeType getChangeType() {
        return this.changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
        if(changeType != ChangeType.Watermark && changeType != ChangeType.Resize) {
            this.stringValue = String.valueOf(this.value);
        }
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public void setStringValue(String value) {
        this.stringValue = value;
    }

    public enum ChangeType {
        None,
        Hue,
        Saturation,
        Brightness,
        Rotate,
        Resize,
        Watermark
    }
}
