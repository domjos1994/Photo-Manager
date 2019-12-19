package de.domjos.photo_manager.services;

import de.domjos.photo_manager.controller.subController.EditController;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.TemporaryEdited;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.util.List;

public final class HistoryTask extends ParentTask<BufferedImage> {
    private List<TemporaryEdited> temporaryEditedList;
    private long id;
    private Image image;
    private Image currentImage;

    public HistoryTask(ProgressBar progressBar, Label messages, long id, List<TemporaryEdited> temporaryEditedList, Image image, Image currentImage) {
        super(progressBar, messages);

        this.id = id;
        this.temporaryEditedList = temporaryEditedList;
        this.image = image;
        this.currentImage = currentImage;
    }

    @Override
    BufferedImage runBody() {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        if(currentImage!=null && image!=null) {
            int hue = 100, saturation = 100, brightness = 100, rotation = 0, width = 0, height = 0;
            String watermark = "", filter = "";

            for(TemporaryEdited temp : this.temporaryEditedList) {
                if(temp.getChangeType()!=null) {
                    switch (temp.getChangeType()) {
                        case Hue:
                            hue = (int) temp.getValue();
                            break;
                        case Saturation:
                            saturation = (int) temp.getValue();
                            break;
                        case Brightness:
                            brightness = (int) temp.getValue();
                            break;
                        case Rotate:
                            rotation = (int) temp.getValue();
                            break;
                        case Watermark:
                            watermark = temp.getStringValue();
                            break;
                        case Filter:
                            filter = temp.getStringValue();
                            break;
                    }

                    BufferedImage image = SwingFXUtils.fromFXImage(this.image, null);
                    ImageHelper.changeHSB(image, SwingFXUtils.fromFXImage(currentImage, null), hue, saturation, brightness);
                    if(!watermark.trim().isEmpty()) {
                        image = ImageHelper.addWaterMark(image, watermark);
                    }
                    image = ImageHelper.resize(image, currentImage, width, height);
                    bufferedImage = ImageHelper.rotate(image, rotation);

                    ImageHelper.Filter.Type type = EditController.getFilterTypeBySelectedItem(filter);
                    if(type!=null) {
                        bufferedImage  = ImageHelper.addFilter(bufferedImage, type);
                    }

                    if(id== temp.getId()) {
                        break;
                    }
                }
            }
        }
        return bufferedImage;
    }
}
