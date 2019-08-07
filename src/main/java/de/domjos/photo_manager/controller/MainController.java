package de.domjos.photo_manager.controller;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.*;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.DescriptionObject;
import de.domjos.photo_manager.model.gallery.Directory;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.gallery.MetaData;
import de.domjos.photo_manager.services.TinifyTask;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;

import static com.lynden.gmapsfx.javascript.object.MapTypeIdEnum.ROADMAP;

public class MainController implements Initializable {
    private @FXML TabPane tbpMain;
    private @FXML Tab tbMain, tbSettings;
    private @FXML MenuItem menMainSettings, menMainClose, menMainExport, menMainImport, menMainHelp;
    private @FXML MenuItem ctxMainDelete, ctxMainRecreate;

    private @FXML TreeView<Directory> tvMain;
    private @FXML ListView<Image> lvMain;
    private @FXML ImageView ivMain;
    private @FXML AnchorPane pnlMain;

    private @FXML Button cmdMainAddFolder, cmdMainFolder, cmdMainFolderSave, cmdMainImageSave;
    private @FXML CheckBox chkMainRecursive;
    private @FXML TextField txtMainFolderName;
    private @FXML TextField txtMainImageCategory, txtMainImageTags;

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

    private @FXML Label lblMessages;
    private @FXML ProgressBar pbMain;

    private @FXML SettingsController settingsController;
    private long rootID;
    private Directory directory;

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
                String msg = resources.getString("main.image.import");

                Task<Void> loadTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(()->PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.WAIT));
                        updateMessage(msg);
                        updateProgress(0, 1);
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateDirectory(directory, parentId[0], recursive);
                        updateProgress(1, 1);
                        updateMessage("");
                        return null;
                    }
                };
                loadTask.setOnSucceeded(event1 -> Platform.runLater(()->{
                    this.enableFolderControls();
                    this.initTreeView();
                    PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT);
                    this.getProgressBar().progressProperty().unbind();
                    this.lblMessages.textProperty().unbind();
                }));
                loadTask.setOnFailed(event1 -> Platform.runLater(()->{
                    PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT);
                    this.getProgressBar().progressProperty().unbind();
                    this.lblMessages.textProperty().unbind();
                }));
                this.getProgressBar().progressProperty().bind(loadTask.progressProperty());
                this.lblMessages.textProperty().bind(loadTask.messageProperty());
                new Thread(loadTask).start();
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
                        this.ivMain.setImage(new javafx.scene.image.Image(new FileInputStream(img)));
                    } else {
                        this.ivMain.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(newValue.getThumbnail())));
                    }
                    this.fillBarChart();
                    this.fillMetaData();
                    this.fillCategoryAndTags();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.cmdMainImageSave.setOnAction(actionEvent -> {
            try {
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    Image image = this.lvMain.getSelectionModel().getSelectedItem();

                    if(this.txtMainImageCategory.getText().trim().isEmpty()) {
                        DescriptionObject descriptionObject = new DescriptionObject();
                        descriptionObject.setTitle(this.txtMainImageCategory.getText().trim());
                        image.setCategory(descriptionObject);
                    }

                    if(this.txtMainImageTags.getText().trim().isEmpty()) {
                        String tags = this.txtMainImageTags.getText().trim();
                        if(tags.contains(";")) {
                            for(String tag : tags.split(";")) {
                                DescriptionObject descriptionObject = new DescriptionObject();
                                descriptionObject.setTitle(tag);
                                image.getTags().add(descriptionObject);
                            }
                        }
                        if(tags.contains(",")) {
                            for(String tag : tags.split(",")) {
                                DescriptionObject descriptionObject = new DescriptionObject();
                                descriptionObject.setTitle(tag);
                                image.getTags().add(descriptionObject);
                            }
                        }
                    }

                    PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
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
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    tinifyTask = new TinifyTask(this.txtMainTinifyWidth.getText(), this.txtMainTinifyHeight.getText(), this.lvMain.getSelectionModel().getSelectedItem());
                } else {
                    if(!this.tvMain.getSelectionModel().isEmpty()) {
                        tinifyTask = new TinifyTask(this.txtMainTinifyWidth.getText(), this.txtMainTinifyHeight.getText(), this.tvMain.getSelectionModel().getSelectedItem().getValue());
                    }
                }

                if(tinifyTask!=null) {
                    this.getProgressBar().progressProperty().bind(tinifyTask.progressProperty());
                    this.lblMessages.textProperty().bind(tinifyTask.messageProperty());
                    tinifyTask.exceptionProperty().addListener((observable, oldValue, newValue) -> Dialogs.printException(newValue));
                    new Thread(tinifyTask).start();
                    tinifyTask.setOnSucceeded(event1 -> {
                        this.getProgressBar().progressProperty().unbind();
                        this.lblMessages.textProperty().unbind();
                        PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT);
                        if(!this.tvMain.getSelectionModel().isEmpty()) {
                            this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue());
                        }
                    });
                    tinifyTask.setOnFailed(tinifyTask.getOnSucceeded());
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

        this.menMainSettings.setOnAction(event -> this.tbpMain.getSelectionModel().select(tbSettings));
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
    }

    private void initBindings() {
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.cmdMainFolder.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.txtMainFolderName.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.chkMainRecursive.visibleProperty());

        this.pnlMain.widthProperty().addListener((observable, oldValue, newValue) -> {
            int newWidth = newValue.intValue() - 20;
            this.ivMain.setFitWidth(newWidth);
            this.ivMain.setFitHeight(newWidth);
            this.ivMain.setViewport(new Rectangle2D(0, 0, newWidth, this.ivMain.getFitHeight()));
        });
    }

    void initTinify() {
        this.pnlMainTinify.setVisible(!PhotoManager.GLOBALS.getSetting(Globals.TINY_KEY, "").equals(""));
    }

    private void initTreeView() {
        try {
            Task<TreeItem<Directory>> generateTreeView = new Task<>() {
                @Override
                protected TreeItem<Directory> call() throws Exception {
                    Platform.runLater(()->PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.WAIT));
                    Directory directory = PhotoManager.GLOBALS.getDatabase().getRoot();
                    TreeItem<Directory> root = new TreeItem<>(directory, getIcon());
                    root.setExpanded(true);
                    rootID = directory.getId();
                    addChildren(directory, root);
                    return root;
                }
            };
            generateTreeView.setOnSucceeded(event -> {
                try {
                    TreeItem<Directory> root = generateTreeView.get();
                    Platform.runLater(()->tvMain.setRoot(root));
                } catch (Exception ex) {
                    Platform.runLater(()->Dialogs.printException(ex));
                } finally {
                    Platform.runLater(()->PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT));
                }
            });
            generateTreeView.setOnFailed(event -> {
                Platform.runLater(()->tvMain.setRoot(null));
                Platform.runLater(()->PhotoManager.GLOBALS.getStage().getScene().setCursor(Cursor.DEFAULT));
            });
            new Thread(generateTreeView).start();
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
                    this.imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(name.getThumbnail())));
                    setGraphic(this.imageView);
                }
            }
        });
    }

    private void fillImageList(Directory directory) {
        try {
            if(directory!=null) {
                this.lvMain.getItems().clear();
                for(Image image : PhotoManager.GLOBALS.getDatabase().getImages(directory, false)) {
                    this.lvMain.getItems().add(image);
                }
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void fillMetaData() {
       try {
           // image-date
           MetaData metaData = ImageHelper.readMetaData(this.lvMain.getSelectionModel().getSelectedItem().getPath());
           if(metaData.getOriginal()!=null) {
               this.txtMainMetaDataDate.setText(metaData.getOriginal());
           }

           // gps-data
           this.txtMainMetaDataLongitude.setText(String.valueOf(metaData.getLongitude()));
           this.txtMainMetaDataLatitude.setText(String.valueOf(metaData.getLatitude()));
           if(metaData.getLatitude()!=0 && metaData.getLongitude()!=0) {
               this.gvMainMetaDataLocation.setVisible(true);
               LatLong latLong = new LatLong(metaData.getLatitude(), metaData.getLongitude());

               MapOptions mapOptions = new MapOptions();
               mapOptions.center(latLong)
                       .mapType(ROADMAP)
                       .overviewMapControl(false)
                       .panControl(false)
                       .rotateControl(false)
                       .scaleControl(false)
                       .streetViewControl(false)
                       .zoomControl(false)
                       .zoom(12);
               GoogleMap map = this.gvMainMetaDataLocation.createMap(mapOptions);
               MarkerOptions markerOptions = new MarkerOptions();
               markerOptions.position(latLong);
               map.addMarker(new Marker(markerOptions));
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

    private Node getIcon() {
        return new ImageView(
            new javafx.scene.image.Image(PhotoManager.class.getResourceAsStream("/images/icons/directory.png"), 16, 16, true, true)
        );
    }

    private void addChildren(Directory directory, TreeItem<Directory> parent) {
        for(Directory child : directory.getChildren()) {
            TreeItem<Directory> childItem = new TreeItem<>(child, this.getIcon());
            childItem.setExpanded(true);
            parent.getChildren().add(childItem);
            this.addChildren(child, childItem);
        }
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
}
