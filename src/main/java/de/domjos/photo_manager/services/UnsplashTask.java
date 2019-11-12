package de.domjos.photo_manager.services;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.Helper;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.settings.Globals;
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
    private String url;
    private String query;

    public UnsplashTask(ProgressBar progressBar, Label messages, String query) {
        super(progressBar, messages);
        this.query = query.toLowerCase().replace(" ", "%20").replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").trim();

        String key = PhotoManager.GLOBALS.getDecryptedSetting(Globals.UNSPLASH_KEY, "");
        if(this.query.isEmpty()) {
            this.url = "https://api.unsplash.com/photos?client_id=" + key;
        } else {
            this.url = "https://api.unsplash.com/search/photos?client_id=" + key + "&query=" + this.query;
        }
    }

    @Override
    protected List<Image> runBody() throws Exception {
        List<Image> images = new LinkedList<>();

        JSONArray jsonArray;
        if(this.query.isEmpty()) {
            jsonArray = Helper.readJsonArrayFromUrl(this.url);
        } else {
            JSONObject jsonObject = Helper.readJsonObjectFromUrl(this.url);
            jsonArray = jsonObject.getJSONArray("results");
        }


        this.updateProgress(0, jsonArray.length());
        for(int i = 0; i<=jsonArray.length()-1; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Image image = new Image();
            if(jsonObject.isNull("description")) {
                if(!jsonObject.isNull("alt_description")) {
                    image.setTitle(jsonObject.getString("alt_description"));
                }
            } else {
                image.setTitle(jsonObject.getString("description"));
            }
            DescriptionObject descriptionObject = new DescriptionObject();
            descriptionObject.setTitle("Unsplash");
            image.setCategory(descriptionObject);
            image.setHeight(jsonObject.getInt("height"));
            image.setWidth(jsonObject.getInt("width"));

            JSONObject links = jsonObject.getJSONObject("urls");
            if(links.has("full")) {
                image.getExtended().put("unSplash", links.getString("full"));
            } else {
                image.getExtended().put("unSplash", links.getString("regular"));
            }
            image.getExtended().put("id", jsonObject.getString("id"));

            InputStream inputStream = new URL(links.getString("thumb")).openStream();
            image.setThumbnail(IOUtils.toByteArray(inputStream));
            inputStream.close();

            if(!jsonObject.isNull("categories")) {
                JSONArray tagArray = jsonObject.getJSONArray("categories");
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
}
