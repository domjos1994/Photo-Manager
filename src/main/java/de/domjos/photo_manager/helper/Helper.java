package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.PhotoManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Helper {

    public static String readResource(String res) throws Exception {
        InputStream stream = PhotoManager.class.getResourceAsStream(res);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine())!=null) {
            builder.append(line);
            builder.append("\n");
        }
        return builder.toString();
    }

    public static JSONArray readJsonArrayFromUrl(String url) throws Exception {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONArray(jsonText);
        }
    }

    public static JSONObject readJsonObjectFromUrl(String url) throws Exception {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
