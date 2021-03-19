package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.Helper;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.URLBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public final class UnsplashTask extends ParentTask<List<Image>> {
    private final String url;
    private final String query;
    private final URLBuilder SEARCH_URL;
    private int max_pages = 1;
    private int page = 1;

    private final static String ROOT_URL = "https://api.unsplash.com";
    private final static String SEARCH_PATH = "search";
    private final static String PHOTOS_PATH = "photos";
    private final static String KEY_PARAM = "client_id";
    private final static String PAGE_PARAM = "page";
    private final static String QUERY_PARAM = "query";


    private final static String UNSPLASH = "unSplash";
    private final static String DESC = "description";
    private final static String ALT_DESC = "alt_description";
    private final static String CATEGORIES = "categories";

    public UnsplashTask(ProgressBar progressBar, Label messages, String query, int page) {
        super(progressBar, messages);
        this.query = query;
        this.page = page;

        String key = PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_KEY, "");
        this.SEARCH_URL = new URLBuilder(UnsplashTask.ROOT_URL);
        this.SEARCH_URL.add(UnsplashTask.SEARCH_PATH);
        this.SEARCH_URL.add(UnsplashTask.PHOTOS_PATH);
        this.SEARCH_URL.addParam(UnsplashTask.KEY_PARAM, key);
        this.SEARCH_URL.addParam(UnsplashTask.PAGE_PARAM, page);
        this.SEARCH_URL.addParam(UnsplashTask.QUERY_PARAM, this.query);

        URLBuilder DISCOVER_URL = new URLBuilder(UnsplashTask.ROOT_URL);
        DISCOVER_URL.add(UnsplashTask.PHOTOS_PATH);
        DISCOVER_URL.addParam(UnsplashTask.KEY_PARAM, key);
        DISCOVER_URL.addParam(UnsplashTask.PAGE_PARAM, page);

        if(this.query.isEmpty()) {
            this.url = DISCOVER_URL.toString();
        } else {
            this.url = this.SEARCH_URL.toString();
        }
    }

    @Override
    protected List<Image> runBody() throws Exception {
        List<Image> images = new LinkedList<>();

        String msg = this.checkKey();
        if(!msg.equals("")) {
            throw new Exception(msg);
        }

        JSONArray jsonArray;
        if(this.query.isEmpty()) {
            jsonArray = Helper.readJsonArrayFromUrl(this.url);
            this.max_pages = 1;
        } else {
            JSONObject jsonObject = Helper.readJsonObjectFromUrl(this.url);
            if(jsonObject.has("total_pages")) {
                this.max_pages = jsonObject.getInt("total_pages");
            }
            jsonArray = jsonObject.getJSONArray("results");
        }

        this.updateProgress(0, jsonArray.length());
        for(int i = 0; i<=jsonArray.length()-1; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Image image = new Image();
            if(jsonObject.isNull(UnsplashTask.DESC)) {
                if(!jsonObject.isNull(UnsplashTask.ALT_DESC)) {
                    image.setTitle(jsonObject.getString(UnsplashTask.ALT_DESC));
                }
            } else {
                image.setTitle(jsonObject.getString(UnsplashTask.DESC));
            }
            DescriptionObject descriptionObject = new DescriptionObject();
            descriptionObject.setTitle(UnsplashTask.UNSPLASH);
            image.setCategory(descriptionObject);
            image.setHeight(jsonObject.getInt("height"));
            image.setWidth(jsonObject.getInt("width"));

            JSONObject links = jsonObject.getJSONObject("urls");
            if(links.has("full")) {
                image.getExtended().put(UnsplashTask.UNSPLASH, links.getString("full"));
            } else {
                image.getExtended().put(UnsplashTask.UNSPLASH, links.getString("regular"));
            }
            image.getExtended().put("id", jsonObject.getString("id"));

            InputStream inputStream = new URL(links.getString("thumb")).openStream();
            image.setThumbnail(IOUtils.toByteArray(inputStream));
            inputStream.close();

            if(!jsonObject.isNull(UnsplashTask.CATEGORIES)) {
                JSONArray tagArray = jsonObject.getJSONArray(UnsplashTask.CATEGORIES);
                for(int j = 0; j<=tagArray.length()-1; j++) {
                    DescriptionObject tagObject = new DescriptionObject();
                    tagObject.setTitle(tagArray.getString(j));
                    image.getTags().add(tagObject);
                }
            }
            images.add(image);
            updateProgress(i + 1, jsonArray.length());
        }
        return images;
    }

    public int getMaxPages() {
        return this.max_pages;
    }

    public int getPage() {
        return this.page;
    }

    private String checkKey() {
        try {
            String url = this.SEARCH_URL.toString();
            JSONObject jsonObject = Helper.readJsonObjectFromUrl(url);
            if(jsonObject.isEmpty()) {
                throw new Exception();
            }
        } catch (Exception ex) {
            return PhotoManager.GLOBALS.getLanguage().getString("main.image.unsplash.key.error");
        }
        return "";
    }
}
