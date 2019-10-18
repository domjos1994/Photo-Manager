package de.domjos.photo_manager.services;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.utils.Dialogs;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.net.ProxySelector;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class WebDav {
    private String baseUrl;
    private Sardine sardine;
    private List<DavResource> davResources;

    public WebDav(String userName, String password, String baseUrl) {
        try {
            this.sardine = new SardineImpl(userName, password) {
                @Override protected HttpClientBuilder
                configure(ProxySelector selector, CredentialsProvider credentials) {
                    return super.configure (selector, credentials).setConnectionReuseStrategy (NoConnectionReuseStrategy.INSTANCE);
                }
            };
            this.davResources = new LinkedList<>();
            this.baseUrl = baseUrl;
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    public boolean testConnection() {
        try {
            this.readDirectory("");
            return !this.davResources.isEmpty();
        } catch (Exception ex) {
            return false;
        }
    }

    public void put(DavResource davResource, Image image) throws Exception {
        File file = new File(image.getPath());
        if(file.exists()) {
            this.sardine.put(this.getBaseUrl() + davResource.getPath() + file.getName(), new FileInputStream(file));
        }
    }

    public void readDirectory(String directory) throws Exception {
        this.davResources.clear();
        this.davResources.addAll(sardine.list(directory == null ? this.baseUrl : directory.isEmpty() ? this.baseUrl : directory));
    }

    public String getBaseUrl() throws Exception {
        String fullUrl = this.baseUrl;
        URL url = new URL(fullUrl);
        return url.getProtocol() + "://" + url.getHost() + "/";
    }

   public List<DavResource> listDirectoriesRecursive() {
        return this.davResources;
   }
}
