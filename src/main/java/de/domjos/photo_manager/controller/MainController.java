package de.domjos.photo_manager.controller;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.LatLong;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.helper.MapHelper;
import de.domjos.photo_manager.model.gallery.*;
import de.domjos.photo_manager.services.RecreateTask;
import de.domjos.photo_manager.services.SaveFolderTask;
import de.domjos.photo_manager.services.TinifyTask;
import de.domjos.photo_manager.services.TreeViewTask;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MainController implements Initializable {
    private @FXML TabPane tbpMain;
    private @FXML Tab tbMain, tbSettings, tbApi, tbMap, tbHelp;
    private @FXML MenuItem menMainSettings, menMainClose, menMainApi, menMainMap, menMainHelp;
    private @FXML MenuItem ctxMainDelete, ctxMainRecreate;

    private @FXML TreeView<Directory> tvMain;
    private @FXML ListView<Image> lvMain;
    private @FXML ImageView ivMainImage, ivMainPreview;

    private @FXML Slider slMainImageZoom;
    private @FXML Label lblMainImageZoom;
    private @FXML Button cmdMainImageZoom;

    private @FXML Button cmdMainAddFolder, cmdMainFolder, cmdMainFolderSave, cmdMainImageSave;
    private @FXML CheckBox chkMainRecursive;
    private @FXML TextField txtMainFolderName;
    private @FXML TextField txtMainImageCategory, txtMainImageTags;

    private @FXML Button cmdMainImageSearch;
    private @FXML TextField txtMainImageSearch;

    private @FXML BarChart<String, Integer> bcMainHistogram;
    private @FXML CheckBox chkMainHistogram, chkMainHistogramRed, chkMainHistogramGreen, chkMainHistogramBlue;

    private @FXML TextField txtMainMetaDataDate;
    private @FXML TextField txtMainMetaDataLongitude, txtMainMetaDataLatitude;
    private @FXML TextField txtMainMetaDataDPIX, txtMainMetaDataDPIY, txtMainMetaDataPXX, txtMainMetaDataPXY;
    private @FXML TextField txtMainMetaDataISO, txtMainMetaDataAperture, txtMainMetaDataExposureTime;
    private @FXML TextField txtMainMetaDataSoftware, txtMainMetaDataCamera;
    private @FXML GoogleMapView gvMainMetaDataLocation;

    private @FXML TitledPane pnlMainTinify, pnlMainImage;
    private @FXML TextField txtMainTinifyWidth, txtMainTinifyHeight;
    private @FXML Button cmdMainTinifyUpload;

    private @FXML Slider slMainSaturation, slMainHue, slMainBrightness;
    private @FXML Button cmdMainImageEditSave;
    private @FXML TableView<TemporaryEdited> tblMainImageHistory;

    private @FXML Label lblMessages;
    private @FXML ProgressBar pbMain;

    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML SettingsController settingsController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML ApiController apiController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML MapController mapController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML HelpController helpController;

    private long rootID;
    private Directory directory;
    private javafx.scene.image.Image currentImage;
    private BufferedImage previewImage, originalPreview;

    public void initialize(URL location, ResourceBundle resources) {
        this.initControllers();
        this.initBindings();
        this.initTinify();
        this.initTreeView();
        this.initListView();

        this.cmdMainAddFolder.setOnAction(event -> this.enableFolderControls());
        this.cmdMainFolder.setOnAction(event -> {
            List<File> paths = Dialogs.printFileChooser(resources.getString("main.dir.path"), true, true, false, null);
            if(paths!=null) {
                if(!paths.isEmpty()) {
                    this.directory.setPath(paths.get(0).getAbsolutePath());
                }
            }
        });
        this.cmdMainFolderSave.setOnAction(event -> {
            try {
                directory.setName(txtMainFolderName.getText());
                final long[] parentId = {rootID};
                if(!tvMain.getSelectionModel().isEmpty()) {
                    parentId[0] = tvMain.getSelectionModel().getSelectedItem().getValue().getId();
                }
                boolean recursive = chkMainRecursive.isSelected();
                directory.setRecursive(recursive);
                directory.setLibrary(true);
                String msg = resources.getString("main.image.import");

                SaveFolderTask saveFolderTask = new SaveFolderTask(this.pbMain, this.lblMessages, msg, this.directory, parentId[0], recursive);
                saveFolderTask.onFinish(()->{
                    this.enableFolderControls();
                    this.initTreeView();
                });
                new Thread(saveFolderTask).start();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.tvMain.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue!=null) {
                    this.fillImageList(newValue.getValue());
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.lvMain.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue !=null) {
                    File img = new File(newValue.getPath());
                    if(img.exists()) {
                        this.fillImage(new javafx.scene.image.Image(new FileInputStream(img)));
                    } else {
                        this.fillImage(new javafx.scene.image.Image(new ByteArrayInputStream(newValue.getThumbnail())));
                    }

                    this.reloadHistory(newValue.getId());
                    this.fillBarChart();
                    this.fillMetaData();
                    this.fillCategoryAndTags();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.slMainImageZoom.valueProperty().addListener((observable, oldValue, newValue) -> this.lblMainImageZoom.setText(newValue.intValue() + " %"));

        this.cmdMainImageZoom.setOnAction(event -> {
            // scale image
            if(!this.lvMain.getSelectionModel().isEmpty() && this.currentImage!=null) {
                Image image = this.lvMain.getSelectionModel().getSelectedItem();
                int width = (int) (image.getWidth() * (this.slMainImageZoom.getValue() / 100.0));
                int height = (int) (image.getHeight() * (this.slMainImageZoom.getValue() / 100.0));
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(this.currentImage, null);
                bufferedImage = ImageHelper.scale(bufferedImage, width, height);
                this.currentImage = SwingFXUtils.toFXImage(bufferedImage, null);
                this.ivMainImage.setImage(this.currentImage);
            }
        });

        this.ivMainImage.setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY:
                    if(this.slMainImageZoom.getValue()>=11) {
                        this.slMainImageZoom.setValue(this.slMainImageZoom.getValue()-10);
                        this.cmdMainImageZoom.fire();
                    }
                    break;
                case SECONDARY:
                    if(this.slMainImageZoom.getValue()<=90) {
                        this.slMainImageZoom.setValue(this.slMainImageZoom.getValue()+10);
                        this.cmdMainImageZoom.fire();
                    }
                    break;
            }
        });

        this.cmdMainImageSave.setOnAction(actionEvent -> {
            try {
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    Image image = this.lvMain.getSelectionModel().getSelectedItem();

                    if(!this.txtMainImageCategory.getText().trim().isEmpty()) {
                        DescriptionObject descriptionObject = new DescriptionObject();
                        descriptionObject.setTitle(this.txtMainImageCategory.getText().trim());
                        image.setCategory(descriptionObject);
                    }

                    if(!this.txtMainImageTags.getText().trim().isEmpty()) {
                        String tags = this.txtMainImageTags.getText().trim();
                        if(tags.contains(";")) {
                            for(String tag : tags.split(";")) {
                                DescriptionObject descriptionObject = new DescriptionObject();
                                descriptionObject.setTitle(tag);
                                image.getTags().add(descriptionObject);
                            }
                        } else if(tags.contains(",")) {
                            for(String tag : tags.split(",")) {
                                DescriptionObject descriptionObject = new DescriptionObject();
                                descriptionObject.setTitle(tag);
                                image.getTags().add(descriptionObject);
                            }
                        } else {
                            if(!tags.trim().isEmpty()) {
                                DescriptionObject descriptionObject = new DescriptionObject();
                                descriptionObject.setTitle(tags.trim());
                                image.getTags().add(descriptionObject);
                            }
                        }
                    }

                    PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
                    Dialogs.printNotification(Alert.AlertType.INFORMATION, resources.getString("main.image.saved"), resources.getString("main.image.saved"));
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.chkMainHistogram.selectedProperty().addListener((observable)-> this.fillBarChart());
        this.chkMainHistogramRed.selectedProperty().addListener((observable)-> this.fillBarChart());
        this.chkMainHistogramGreen.selectedProperty().addListener((observable)-> this.fillBarChart());
        this.chkMainHistogramBlue.selectedProperty().addListener((observable)-> this.fillBarChart());

        this.cmdMainTinifyUpload.setOnAction(event -> {
            try {
                TinifyTask tinifyTask = null;
                String width = this.txtMainTinifyWidth.getText();
                String height = this.txtMainTinifyHeight.getText();
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    tinifyTask = new TinifyTask(this.pbMain, this.lblMessages, width, height, this.lvMain.getSelectionModel().getSelectedItem());
                } else {
                    if(!this.tvMain.getSelectionModel().isEmpty()) {
                        tinifyTask = new TinifyTask(this.pbMain, this.lblMessages, width, height, this.tvMain.getSelectionModel().getSelectedItem().getValue());
                    }
                }

                if(tinifyTask!=null) {
                    new Thread(tinifyTask).start();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.slMainHue.valueProperty().addListener((observable, oldValue, newValue) -> this.ivMainPreview.setImage(this.editImage()));
        this.slMainSaturation.valueProperty().addListener((observable, oldValue, newValue) -> this.ivMainPreview.setImage(this.editImage()));
        this.slMainBrightness.valueProperty().addListener((observable, oldValue, newValue) -> this.ivMainPreview.setImage(this.editImage()));

        this.tblMainImageHistory.getSelectionModel().selectedItemProperty().addListener((observableValue, temporaryEdited, t1) -> {
            if(!this.tblMainImageHistory.getSelectionModel().isEmpty()) {
                long id = this.tblMainImageHistory.getSelectionModel().getSelectedItem().getId();
                int hue = 100, saturation = 100, brightness = 100;

                for(TemporaryEdited temp : this.tblMainImageHistory.getItems()) {
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
                        }

                        BufferedImage image = SwingFXUtils.fromFXImage(this.ivMainImage.getImage(), null);
                        ImageHelper.changeHSB(image, SwingFXUtils.fromFXImage(this.currentImage, null),
                                hue, saturation, brightness);
                        System.out.println(hue + ": " + saturation + ": " + brightness);
                        this.ivMainImage.setImage(SwingFXUtils.toFXImage(image, null));

                        if(id== temp.getId()) {
                            break;
                        }
                    }
                }
            }
        });

        this.cmdMainImageEditSave.setOnAction(actionEvent -> {
            try {
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    long id = this.lvMain.getSelectionModel().getSelectedItem().getId();
                    if(this.slMainHue.getValue()!=100) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Hue);
                        temporaryEdited.setValue(this.slMainHue.getValue());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(this.slMainSaturation.getValue()!=100) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Saturation);
                        temporaryEdited.setValue(this.slMainSaturation.getValue());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }
                    if(this.slMainBrightness.getValue()!=100) {
                        TemporaryEdited temporaryEdited = new TemporaryEdited();
                        temporaryEdited.setChangeType(TemporaryEdited.ChangeType.Brightness);
                        temporaryEdited.setValue(this.slMainBrightness.getValue());
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateEdited(temporaryEdited, id);
                    }

                    this.reloadHistory(id);
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainDelete.setOnAction(event -> {
            try {
                if(!this.tvMain.getSelectionModel().isEmpty()) {
                    Directory directory = this.tvMain.getSelectionModel().getSelectedItem().getValue();
                    if(!directory.isRoot()) {
                        PhotoManager.GLOBALS.getDatabase().deleteDirectory(directory);
                        this.initTreeView();
                    }
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainRecreate.setOnAction(event -> {
            if(!this.tvMain.getSelectionModel().isEmpty()) {
                List<Image> images = new LinkedList<>();
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    images.add(this.lvMain.getSelectionModel().getSelectedItem());
                } else {
                    images.addAll(this.lvMain.getItems());
                }

                RecreateTask recreateTask = new RecreateTask(this.pbMain, this.lblMessages, images);
                recreateTask.onFinish(()->this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue()));
                new Thread(recreateTask).start();
            }
        });

        this.cmdMainImageSearch.setOnAction(event -> search());

        this.menMainSettings.setOnAction(event -> this.tbpMain.getSelectionModel().select(this.tbSettings));
        this.menMainApi.setOnAction(event -> this.tbpMain.getSelectionModel().select(this.tbApi));
        this.menMainMap.setOnAction(event -> {
            this.mapController.init();
            this.tbpMain.getSelectionModel().select(this.tbMap);
        });
        this.menMainHelp.setOnAction(event -> this.tbpMain.getSelectionModel().select(this.tbHelp));
        this.menMainClose.setOnAction(event -> Platform.exit());
    }

    void back() {
        this.tbpMain.getSelectionModel().select(this.tbMain);
    }

    ProgressBar getProgressBar() {
        return this.pbMain;
    }

    void setMessage(String msg) {
        this.lblMessages.setText(msg);
    }

    private void initControllers() {
        this.settingsController.init(this);
        this.apiController.init(this);
        this.mapController.init(this);
        this.helpController.init(this);
    }

    private void reloadHistory(long id) throws Exception {
        this.tblMainImageHistory.getItems().clear();
        for(TemporaryEdited temporaryEdited : PhotoManager.GLOBALS.getDatabase().getTemporaryEdited(id)) {
            this.tblMainImageHistory.getItems().add(temporaryEdited);
        }
    }

    private void initBindings() {
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.cmdMainFolder.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.txtMainFolderName.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.chkMainRecursive.visibleProperty());

        TableColumn<TemporaryEdited, String> changeType = new TableColumn<>("ChangeType");
        changeType.setText(PhotoManager.GLOBALS.getLanguage().getString("main.image.history.type"));
        changeType.setCellValueFactory(new PropertyValueFactory<>("changeType"));
        TableColumn<TemporaryEdited, String>  value = new TableColumn<>("Value");
        value.setText(PhotoManager.GLOBALS.getLanguage().getString("main.image.history.value"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        this.tblMainImageHistory.getColumns().add(changeType);
        this.tblMainImageHistory.getColumns().add(value);
    }

    void initTinify() {
        this.pnlMainTinify.setVisible(!PhotoManager.GLOBALS.getSetting(Globals.TINY_KEY, "").equals(""));
    }

    private void initTreeView() {
        try {
            TreeViewTask treeViewTask = new TreeViewTask(this.pbMain, this.lblMessages);
            treeViewTask.onFinish(()->{
                try {
                    this.rootID = PhotoManager.GLOBALS.getDatabase().getRoot().getId();
                    TreeItem<Directory> root = treeViewTask.get();
                    this.tvMain.setRoot(root);
                } catch (Exception ex) {
                    Dialogs.printException(ex);
                }
            });
            new Thread(treeViewTask).start();
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void initListView() {
        this.lvMain.setCellFactory(param -> new ListCell<>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(Image name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(name.getName());
                    javafx.scene.image.Image image = new javafx.scene.image.Image(new ByteArrayInputStream(name.getThumbnail()));
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    if(bufferedImage!=null) {
                        this.imageView.setImage(SwingFXUtils.toFXImage(ImageHelper.scale(bufferedImage, 50, 50), null));
                        setGraphic(this.imageView);
                    }
                }
            }
        });
    }

    private void enableFolderControls() {
        if(this.cmdMainFolder.isVisible()) {
            this.txtMainFolderName.setText("");
            this.cmdMainFolder.setVisible(false);
        } else {
            this.directory = new Directory();
            this.cmdMainFolder.setVisible(true);
        }
    }

    private void search() {
        if(!this.tvMain.getSelectionModel().isEmpty()) {
            this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue(), this.txtMainImageSearch.getText().toLowerCase().trim());
        }
    }

    private void fillImage(javafx.scene.image.Image image) {
        this.currentImage = image;
        Image img = this.lvMain.getSelectionModel().getSelectedItem();
        javafx.scene.image.Image preview = new javafx.scene.image.Image(new ByteArrayInputStream(img.getThumbnail()));
        this.originalPreview = SwingFXUtils.fromFXImage(preview, null);
        this.previewImage = ImageHelper.deepCopy(this.originalPreview);
        this.ivMainPreview.setImage(preview);
        this.ivMainImage.setFitWidth(image.getWidth());
        this.ivMainImage.setFitHeight(image.getHeight());
        this.ivMainImage.setViewport(new Rectangle2D(0, 0, image.getWidth(), image.getHeight()));
        this.ivMainImage.setPreserveRatio(true);
        this.ivMainImage.setImage(image);
        this.slMainImageZoom.setValue(100.0);
    }

    private javafx.scene.image.Image editImage() {
        // change saturation
        int hue = (int) this.slMainHue.getValue();
        int saturation = (int) this.slMainSaturation.getValue();
        int brightness = (int) this.slMainBrightness.getValue();

        if(this.originalPreview!=null && this.previewImage!=null) {
            ImageHelper.changeHSB(this.previewImage, this.originalPreview, hue, saturation, brightness);
            return SwingFXUtils.toFXImage(this.previewImage, null);
        }

        return null;
    }

    private void fillImageList(Directory directory) {
        this.fillImageList(directory, "");
    }

    private void fillImageList(Directory directory, String search) {
        try {
            search = search.trim();
            if(directory!=null) {
                this.lvMain.getItems().clear();
                for(Image image : PhotoManager.GLOBALS.getDatabase().getImages(directory, false)) {
                    boolean foundItem = true;
                    if(!search.isEmpty()) {
                        foundItem = false;
                        if(image.getCategory()!=null) {
                            System.out.println(image.getCategory().getTitle());
                            if(image.getCategory().getTitle().trim().toLowerCase().contains(search)) {
                                foundItem = true;
                            }
                        }
                        if(!image.getTags().isEmpty()) {
                            for(DescriptionObject descriptionObject : image.getTags()) {
                                System.out.println(descriptionObject.getTitle());
                                if(descriptionObject.getTitle().trim().toLowerCase().contains(search)) {
                                    foundItem = true;
                                }
                            }
                        }
                    }

                    if(foundItem) {
                        this.lvMain.getItems().add(image);
                    }
                }
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void fillMetaData() {
       try {
           // image-date
           Image image = this.lvMain.getSelectionModel().getSelectedItem();
           MetaData metaData = ImageHelper.readMetaData(image.getPath());
           if(metaData.getOriginal()!=null) {
               this.txtMainMetaDataDate.setText(metaData.getOriginal());
           }

           // gps-data
           this.txtMainMetaDataLongitude.setText(String.valueOf(metaData.getLongitude()));
           this.txtMainMetaDataLatitude.setText(String.valueOf(metaData.getLatitude()));
           if(metaData.getLatitude()!=0 && metaData.getLongitude()!=0) {
               this.gvMainMetaDataLocation.setVisible(true);

               MapHelper mapHelper = new MapHelper(this.gvMainMetaDataLocation, new LatLong(metaData.getLatitude(), metaData.getLongitude()));
               mapHelper.init(Collections.singletonList(image));
           } else {
               this.gvMainMetaDataLocation.setVisible(false);
           }

           // resolution-data
           this.txtMainMetaDataDPIX.setText(String.valueOf(metaData.getXResolutionInDPI()));
           this.txtMainMetaDataDPIY.setText(String.valueOf(metaData.getYResolutionInDPI()));
           this.txtMainMetaDataPXX.setText(String.valueOf(metaData.getXResolutionInPX()));
           this.txtMainMetaDataPXY.setText(String.valueOf(metaData.getYResolutionInPX()));

           // exif-data
           this.txtMainMetaDataISO.setText(String.valueOf(metaData.getIso()));
           this.txtMainMetaDataAperture.setText(String.valueOf(metaData.getAperture()));
           this.txtMainMetaDataExposureTime.setText(String.valueOf(metaData.getExposureTime()));
           this.txtMainMetaDataSoftware.setText(metaData.getEditedWith());
           this.txtMainMetaDataCamera.setText(metaData.getCamera());
       } catch (Exception ex) {
           Dialogs.printException(ex);
       }
    }

    private void fillBarChart() {
        try {
            BufferedImage bufferedImage = ImageHelper.getImage(this.lvMain.getSelectionModel().getSelectedItem().getPath());
            this.bcMainHistogram.getData().clear();

            if(bufferedImage!=null) {
                ResourceBundle ln = PhotoManager.GLOBALS.getLanguage();

                Map<Integer, String> typeMap = new LinkedHashMap<>();
                if(this.chkMainHistogram.isSelected()) {
                    typeMap.put(0, ln.getString("main.image.histogram"));
                }
                if(this.chkMainHistogramRed.isSelected()) {
                    typeMap.put(1, ln.getString("main.image.histogram.red"));
                }
                if(this.chkMainHistogramGreen.isSelected()) {
                    typeMap.put(2, ln.getString("main.image.histogram.green"));
                }
                if(this.chkMainHistogramBlue.isSelected()) {
                    typeMap.put(3, ln.getString("main.image.histogram.blue"));
                }

                for(Map.Entry<Integer, String> entry : typeMap.entrySet()) {
                    int[] histogram = ImageHelper.getHistogram(bufferedImage, entry.getKey());

                    XYChart.Series<String, Integer> series = new XYChart.Series<>();
                    series.setName(entry.getValue());
                    List<XYChart.Data<String, Integer>> data = new LinkedList<>();
                    for(int j = 0; j<=histogram.length-1; j++) {
                        data.add(new XYChart.Data<>(String.valueOf(j), histogram[j]));
                    }
                    series.getData().addAll(data);
                    this.bcMainHistogram.getData().add(series);
                }
            }
            this.bcMainHistogram.setLegendVisible(true);
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void fillCategoryAndTags() {
        if(!this.lvMain.getSelectionModel().isEmpty()) {
            Image image = this.lvMain.getSelectionModel().getSelectedItem();
            if(image!=null) {
                this.pnlMainImage.setText(image.getName());
                if(image.getCategory()!=null) {
                    this.txtMainImageCategory.setText(image.getCategory().getTitle());
                } else {
                    this.txtMainImageCategory.setText("");
                }
                StringBuilder stringBuilder = new StringBuilder();
                for(DescriptionObject descriptionObject : image.getTags()) {
                    stringBuilder.append(descriptionObject.getTitle());
                    stringBuilder.append(", ");
                }
                this.txtMainImageTags.setText(stringBuilder.toString());
            }
        }
    }
}
