package de.domjos.photo_manager.services;

import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramTagFeedRequest;
import org.brunocvcunha.instagram4j.requests.InstagramUploadPhotoRequest;
import org.brunocvcunha.instagram4j.requests.payload.ImageMeta;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedItem;
import org.brunocvcunha.instagram4j.requests.payload.InstagramFeedResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramLoginResult;

import javax.imageio.ImageIO;

public class InstagramTask extends ParentTask<List<Image>> {
    private final String user, pwd, search;
    private final int page;
    private final Image image;

    public InstagramTask(ProgressBar progressBar, Label messages, String userName, String password, String search, int page) {
        super(progressBar, messages);

        this.user = userName;
        this.pwd = password;
        this.search = search;
        this.page = page;
        this.image = null;
    }

    public InstagramTask(ProgressBar progressBar, Label messages, String userName, String password, Image image) {
        super(progressBar, messages);

        this.user = userName;
        this.pwd = password;
        this.search = "";
        this.page = 0;
        this.image = image;
    }

    @Override
    public List<Image> runBody() throws Exception {
        List<Image> images = new LinkedList<>();

        Instagram4j instagram = Instagram4j.builder().username(this.user).password(this.pwd).build();
        instagram.setup();
        InstagramLoginResult result = instagram.login();

        if(!result.getStatus().toLowerCase().contains("fail")) {
            if(this.image == null) {
                InstagramTagFeedRequest instagramTagFeedRequest = new InstagramTagFeedRequest(this.search);
                InstagramFeedResult instagramFeedResult = instagram.sendRequest(instagramTagFeedRequest);
                int max = instagramFeedResult.getNum_results();
                int progress = 0;
                this.updateProgress(progress, max);
                for(InstagramFeedItem item : instagramFeedResult.getItems()) {
                    if((this.page * 50) <= progress && ((this.page + 1) * 50) >= progress) {
                        if(item != null) {
                            if(item.image_versions2 != null) {
                                if(item.image_versions2.candidates != null) {
                                    List<ImageMeta> metas = item.image_versions2.candidates;
                                    if(metas.size() != 0) {
                                        ImageMeta meta = metas.get(0);
                                        Image image = new Image();
                                        image.setTitle(item.code);
                                        image.setWidth(meta.width);
                                        image.setHeight(meta.height);
                                        image.setPath(meta.url);
                                        BufferedImage img = ImageIO.read(new URL(meta.url));
                                        image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(img, 200, 200)));
                                        images.add(image);
                                    }
                                }
                            }
                        }
                    }
                    progress++;
                    this.updateProgress(progress, max);
                }
            } else {
                this.updateProgress(0, 1);
                StringBuilder tags = new StringBuilder();
                for(DescriptionObject descriptionObject : image.getTags()) {
                    tags.append("#").append(descriptionObject.getTitle().trim()).append(" ");
                }

                instagram.sendRequest(new InstagramUploadPhotoRequest(new File(image.getPath()), image.getTitle() + " " + tags.toString().trim()));
                this.updateProgress(1, 1);
            }
        }

        return images;
    }
}
