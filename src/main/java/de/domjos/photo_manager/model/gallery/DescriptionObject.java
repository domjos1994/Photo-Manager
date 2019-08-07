package de.domjos.photo_manager.model.gallery;

public class DescriptionObject {
    private long id;
    private String title;
    private String description;

    public DescriptionObject() {
        this.id = 0;
        this.title = "";
        this.description = "";
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
