package de.domjos.photo_manager.model.objects;

public class BaseObject {
    private long id;
    private String title;

    public BaseObject() {
        super();

        this.id = 0;
        this.title = "";
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
}
