package de.domjos.photo_manager.utils;

import org.sqlite.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class URLBuilder {
    private final String ROOT;
    private String url;
    private final List<String> params;

    public URLBuilder(String root) {
        this.ROOT = root;
        this.url = this.ROOT;
        this.params = new LinkedList<>();
    }

    public String getRoot() {
        return this.ROOT;
    }

    public void add(String path) {
        this.url += "/" + path;
    }

    public void addParam(String name, Object value) {
        if(value instanceof String) {
            String encodedValue = ((String) value).toLowerCase().replace(" ", "%20").replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").trim();
            this.params.add(name + "=" + encodedValue);
        } else {
            this.params.add(name + "=" + value);
        }
    }

    @Override
    public String toString() {
        String tmp = "";
        if(!this.params.isEmpty()) {
            tmp = this.url + "?" + StringUtils.join(this.params, "&");
        }
        return tmp;
    }
}
