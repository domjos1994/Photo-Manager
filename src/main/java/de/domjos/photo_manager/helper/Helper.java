package de.domjos.photo_manager.helper;

import de.domjos.photo_manager.PhotoManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
}
