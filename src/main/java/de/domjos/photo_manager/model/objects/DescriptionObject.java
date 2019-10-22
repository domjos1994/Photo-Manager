package de.domjos.photo_manager.model.objects;

public class DescriptionObject extends BaseObject {
    private String description;

    public DescriptionObject() {
        super();

        this.description = "";
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
