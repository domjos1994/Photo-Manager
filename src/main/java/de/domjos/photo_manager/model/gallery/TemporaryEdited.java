package de.domjos.photo_manager.model.gallery;

public class TemporaryEdited {
    private long id;
    private ChangeType changeType;
    private double value;

    public TemporaryEdited() {
        this.id = 0;
        this.changeType = null;
        this.value = 0.0;
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
    }


    public enum ChangeType {
        None,
        Hue,
        Saturation,
        Brightness,
        Width,
        Height,
        Rotate
    }
}
