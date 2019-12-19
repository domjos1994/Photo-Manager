package de.domjos.photo_manager.controller.subController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.Template;
import de.domjos.photo_manager.model.gallery.TemporaryEdited;
import de.domjos.photo_manager.settings.Cache;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.util.ResourceBundle;

public class EditController extends ParentController {
    private @FXML ImageView ivPreview;
    private @FXML Slider slSaturation, slHue, slBrightness, slRotate;
    private @FXML TextField txtWatermark;
    private @FXML CheckBox chkResize;
    private @FXML TextField txtResizeWidth;
    private @FXML TextField txtResizeHeight;
    private @FXML Button cmdSave;
    private @FXML ComboBox<String> cmbFilter;
    private Cache cache;

    public ImageView getPreview() {
        return this.ivPreview;
    }

    public TextField getWatermark() {
        return this.txtWatermark;
    }

    public void setCache(Cache cache) {
        this.cache = cache;

        if(this.cache.getOriginal()!=null) {
            this.txtResizeWidth.setText(String.valueOf(this.cache.getOriginal().getWidth()));
            this.txtResizeHeight.setText(String.valueOf(this.cache.getOriginal().getHeight()));
        }
    }

    public Template updateTemplate(Template template) {
        template.getPreferences().put(Template.Preference.HUE.toString(), String.valueOf(this.slHue.getValue()));
        template.getPreferences().put(Template.Preference.BRIGHTNESS.toString(), String.valueOf(this.slBrightness.getValue()));
        template.getPreferences().put(Template.Preference.SATURATION.toString(), String.valueOf(this.slSaturation.getValue()));
        template.getPreferences().put(Template.Preference.Rotation.toString(), String.valueOf(this.slRotate.getValue()));
        template.getPreferences().put(Template.Preference.Filter.toString(), this.cmbFilter.getSelectionModel().getSelectedItem());
        return template;
    }

    public void reset() {
        this.slHue.setValue(100.0);
        this.slBrightness.setValue(100.0);
        this.slSaturation.setValue(100.0);
        this.slRotate.setValue(0.0);
        this.txtWatermark.setText("");
        this.txtResizeWidth.setText(String.valueOf(this.cache.getOriginal().getWidth()));
        this.txtResizeHeight.setText(String.valueOf(this.cache.getOriginal().getHeight()));
        this.cmbFilter.getSelectionModel().clearSelection();
    }

    public void setTemplate(Template template) {
        this.slHue.setValue(Double.parseDouble(template.getPreferences().getOrDefault(Template.Preference.HUE.toString(), "100.0")));
        this.slBrightness.setValue(Double.parseDouble(template.getPreferences().getOrDefault(Template.Preference.BRIGHTNESS.toString(), "100.0")));
        this.slSaturation.setValue(Double.parseDouble(template.getPreferences().getOrDefault(Template.Preference.SATURATION.toString(), "100.0")));
        this.slRotate.setValue(Double.parseDouble(template.getPreferences().getOrDefault(Template.Preference.Rotation.toString(), "0.0")));
        this.txtWatermark.setText(template.getPreferences().getOrDefault(Template.Preference.Watermark.toString(), ""));
        this.cmbFilter.getSelectionModel().select(template.getPreferences().getOrDefault(Template.Preference.Filter.toString(), ""));

        String resize = template.getPreferences().getOrDefault(Template.Preference.Resize.toString(), "");
        if(!resize.isEmpty()) {
            String[] content = resize.split(":");
            this.txtResizeWidth.setText(content[0].trim());
            this.txtResizeHeight.setText(content[1].trim());
        }
    }

    public static ImageHelper.Filter.Type getFilterTypeBySelectedItem(String item) {
        ResourceBundle lang = PhotoManager.GLOBALS.getLanguage();
        String keyPart = "main.image.edit.filter.";

        if(item.equals(lang.getString(keyPart + "color"))) {
            return ImageHelper.Filter.Type.ColorFilter;
        }
        if(item.equals(lang.getString(keyPart + "invert"))) {
            return ImageHelper.Filter.Type.InvertFilter;
        }
        if(item.equals(lang.getString(keyPart + "sharpen"))) {
            return ImageHelper.Filter.Type.SharpenFilter;
        }
        if(item.equals(lang.getString(keyPart + "blur"))) {
            return ImageHelper.Filter.Type.BlurFilter;
        }
        return null;
    }

    @Override
    public void initialize(ResourceBundle resources) {
        this.txtResizeWidth.disableProperty().bind(this.chkResize.selectedProperty().not());
        this.txtResizeHeight.disableProperty().bind(this.chkResize.selectedProperty().not());
        this.reloadFilter();

        this.slHue.valueProperty().addListener((observable, oldValue, newValue) -> this.ivPreview.setImage(this.editImage()));
        this.slSaturation.valueProperty().addListener((observable, oldValue, newValue) -> this.ivPreview.setImage(this.editImage()));
        this.slBrightness.valueProperty().addListener((observable, oldValue, newValue) -> this.ivPreview.setImage(this.editImage()));
        this.slRotate.valueProperty().addListener((observable, oldValue, newValue) -> this.ivPreview.setImage(this.editImage()));
        this.txtWatermark.textProperty().addListener((observable, oldValue, newValue) -> this.ivPreview.setImage(this.editImage()));
        this.txtResizeHeight.textProperty().addListener((observable, oldValue, newValue) -> this.ivPreview.setImage(this.editImage()));
        this.txtResizeWidth.textProperty().addListener((observable, oldValue, newValue) -> this.ivPreview.setImage(this.editImage()));
        this.cmbFilter.getSelectionModel().selectedItemProperty().addListener(((observableValue, s, t1) -> this.ivPreview.setImage(this.editImage())));

        this.cmdSave.setOnAction(actionEvent -> {
            try {
                if(!this.mainController.getLvMain().getSelectionModel().isEmpty()) {
                    long id = this.mainController.getLvMain().getSelectionModel().getSelectedItem().getId();
                    if(this.slHue.getValue()!=100) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Hue);
                        temporaryEdited.setValue(this.slHue.getValue());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(this.slSaturation.getValue()!=100) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Saturation);
                        temporaryEdited.setValue(this.slSaturation.getValue());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(this.slBrightness.getValue()!=100) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Brightness);
                        temporaryEdited.setValue(this.slBrightness.getValue());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(this.slRotate.getValue()!=0) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Rotate);
                        temporaryEdited.setValue(this.slRotate.getValue());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(!this.txtWatermark.getText().trim().isEmpty()) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Watermark);
                        temporaryEdited.setStringValue(this.txtWatermark.getText().trim());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(this.chkResize.isSelected()) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Resize);
                        temporaryEdited.setStringValue("W:" + this.txtResizeWidth.getText().trim() + ";H:" + this.txtResizeHeight.getText().trim());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(!this.cmbFilter.getSelectionModel().isEmpty()) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Filter);
                        temporaryEdited.setStringValue(this.cmbFilter.getSelectionModel().getSelectedItem());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }

                    this.mainController.historyController.reloadHistory(id);
                }

                ResourceBundle lang = PhotoManager.GLOBALS.getLanguage();
                Dialogs.printNotification(Alert.AlertType.INFORMATION, lang.getString("settings.saved"), lang.getString("settings.saved"));
                this.reset();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
    }

    private void reloadFilter() {
        this.cmbFilter.getItems().clear();

        for(ImageHelper.Filter.Type type : ImageHelper.Filter.Type.values()) {
            String name = type.name().trim().toLowerCase().replace("filter", "");
            this.cmbFilter.getItems().add(PhotoManager.GLOBALS.getLanguage().getString("main.image.edit.filter." + name));
        }
    }

    private javafx.scene.image.Image editImage() {
        // change saturation
        int hue = (int) this.slHue.getValue();
        int saturation = (int) this.slSaturation.getValue();
        int brightness = (int) this.slBrightness.getValue();
        int rotation = (int) this.slRotate.getValue();
        String waterMark = this.txtWatermark.getText();
        String strWidth = this.txtResizeWidth.getText().trim();
        String strHeight = this.txtResizeHeight.getText().trim();

        if(this.cache.getOriginalPreview()!=null && this.cache.getPreviewImage()!=null) {
            ImageHelper.changeHSB(this.cache.getPreviewImage(), this.cache.getOriginalPreview(), hue, saturation, brightness);
            this.cache.setPreviewImage(ImageHelper.rotate(this.cache.getPreviewImage(), rotation));

            if(!waterMark.trim().isEmpty()) {
                this.cache.setPreviewImage(ImageHelper.addWaterMark(this.cache.getPreviewImage(), waterMark.trim()));
            }
            if(this.chkResize.isSelected()) {
                this.cache.setPreviewImage(ImageHelper.resize(this.cache.getOriginalPreview(), this.cache.getOriginal(), (int) Double.parseDouble(strWidth), (int) Double.parseDouble(strHeight)));
            }
            if(!this.cmbFilter.getSelectionModel().isEmpty()) {
                String selectedFilter = this.cmbFilter.getSelectionModel().getSelectedItem();
                ImageHelper.Filter.Type type = EditController.getFilterTypeBySelectedItem(selectedFilter);
                if(type!=null) {
                    this.cache.setPreviewImage(ImageHelper.addFilter(this.cache.getPreviewImage(), type));
                }
            }
            return SwingFXUtils.toFXImage(this.cache.getPreviewImage(), null);
        }

        return null;
    }
}
