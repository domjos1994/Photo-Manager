package de.domjos.photo_manager.controller;

import com.gluonhq.maps.MapView;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.subController.*;
import de.domjos.photo_manager.helper.ControlsHelper;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.*;
import de.domjos.photo_manager.model.objects.DescriptionObject;
import de.domjos.photo_manager.model.services.Cloud;
import de.domjos.photo_manager.services.*;
import de.domjos.photo_manager.settings.Cache;
import de.domjos.photo_manager.settings.Globals;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MainController implements Initializable {
    private @FXML SplitPane splPaneDirectories, splPaneImages, splPaneImage;

    private @FXML MenuBar menMain;
    private @FXML ToolBar toolbarMain;

    private @FXML TabPane tbpMain;
    private @FXML Tab tbMain, tbSettings, tbMap, tbSlideshow, tbHelp;
    private @FXML MenuItem menMainSettings, menMainClose, menMainMap, menMainHelp;
    private @FXML MenuItem ctxMainDelete, ctxMainRecreate, ctxMainSlideshow;

    private @FXML ContextMenu ctxMainImage;
    private @FXML MenuItem ctxMainImageApply, ctxMainImageSaveAs, ctxMainImageAssemble, ctxMainImageScale;
    private @FXML MenuItem ctxMainImageDelete;
    private @FXML Menu ctxMainImageMove;
    private List<SettingsController.DirRow> dirRows;

    private @FXML TreeView<Directory> tvMain;
    private @FXML ListView<Image> lvMain;
    private @FXML ImageView ivMainImage;

    private @FXML Slider slMainImageZoom;
    private @FXML Label lblMainImageZoom;
    private @FXML Button cmdMainImageZoom;

    private @FXML Button cmdMainAddFolder, cmdMainFolder, cmdMainFolderSave, cmdMainImageSave;
    private @FXML CheckBox chkMainRecursive;
    private @FXML TextField txtMainFolderName;
    private @FXML TextField txtMainImageCategory, txtMainImageTags, txtMainImageName;

    private @FXML Button cmdMainImageSearch;
    private @FXML TextField txtMainImageSearch;

    private @FXML Button cmdMainTemplate, cmdMainTemplateAdd, cmdMainTemplateDelete;
    private @FXML ComboBox<String> cmbMainTemplates;

    private @FXML MapView gvMainServicesLocation;

    private @FXML TitledPane pnlMainImage;

    private @FXML TableView<TemporaryEdited> tblMainImageHistory;

    private @FXML Label lblMessages;
    private @FXML ProgressBar pbMain;

    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML SettingsController settingsController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML MapController mapController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML SlideshowController slideshowController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML HelpController helpController;

    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML HistogramController histogramController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML MetaDataController metaDataController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML TinifyController tinifyController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML UnsplashController unsplashController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML CloudController cloudController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML EditController editController;

    private long rootID;
    private Directory directory;
    private javafx.scene.image.Image currentImage;
    private final Cache cache = new Cache();
    private SaveFolderTask importTask;

    public void initialize(URL location, ResourceBundle resources) {
        this.dirRows = new LinkedList<>();
        this.initControllers();
        this.initBindings();
        this.initTinify();
        this.initTreeView();
        this.initListView();
        this.fillTemplates();
        this.loadSplitPanePositions();

        PhotoManager.GLOBALS.getCloseRunnable().add(() -> {
            if(importTask!=null) {
                if(importTask.isRunning()) {
                    String content = resources.getString("sys.progressMessage");
                    PhotoManager.GLOBALS.setClose(Dialogs.printConfirmDialog(Alert.AlertType.WARNING, "Warning", content, content));
                } else {
                    PhotoManager.GLOBALS.setClose(true);
                }
            } else {
                PhotoManager.GLOBALS.setClose(true);
            }
        });

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
                directory.setTitle(txtMainFolderName.getText());
                final long[] parentId = {rootID};
                if(!tvMain.getSelectionModel().isEmpty()) {
                    parentId[0] = tvMain.getSelectionModel().getSelectedItem().getValue().getId();
                }
                boolean recursive = chkMainRecursive.isSelected();
                directory.setRecursive(recursive);
                directory.setLibrary(true);
                String msg = resources.getString("main.image.import");

                importTask = new SaveFolderTask(this.pbMain, this.lblMessages, msg, this.directory, parentId[0], recursive);
                importTask.onFinish(()->{
                    this.enableFolderControls();
                    this.initTreeView();
                });
                new Thread(importTask).start();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.tvMain.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue!=null) {
                    this.fillImageList(newValue.getValue());
                    this.cloudController.fillCloudWithDefault();
                    this.cloudController.getCloudPath();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.lvMain.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue !=null) {
                    if(this.lblMessages.textProperty().isBound()) {
                        this.lblMessages.textProperty().unbind();
                    }
                    this.lblMessages.setText(String.format(resources.getString("main.image.selected"), this.lvMain.getSelectionModel().getSelectedItems().size(), this.lvMain.getItems().size()));

                    File img = new File(newValue.getPath());
                    if(img.exists()) {
                        FileInputStream fileInputStream = new FileInputStream(img);
                        this.fillImage(new javafx.scene.image.Image(fileInputStream));
                        fileInputStream.close();
                    } else {
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(newValue.getThumbnail());
                        this.fillImage(new javafx.scene.image.Image(byteArrayInputStream));
                        byteArrayInputStream.close();
                    }

                    this.histogramController.setImage(newValue);
                    this.metaDataController.setImage(newValue);
                    this.reloadHistory(newValue.getId());
                    this.fillCategoryAndTags();
                    this.cloudController.fillCloudWithDefault();
                    this.cloudController.getCloudPath();
                    this.editController.setCache(this.cache);

                    this.tblMainImageHistory.getSelectionModel().select(this.tblMainImageHistory.getItems().size() - 1);
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainImage.setOnShowing(event -> {
            this.ctxMainImageAssemble.setDisable(this.lvMain.getSelectionModel().getSelectedItems().size()<=1);
            this.ctxMainImageScale.setDisable(this.lvMain.getSelectionModel().getSelectedItems().size()<=1);
        });

        this.ctxMainImageApply.setOnAction(event -> {
            try {
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    if(this.tblMainImageHistory.getSelectionModel().isEmpty()) {
                        this.tblMainImageHistory.getSelectionModel().select(this.tblMainImageHistory.getItems().size() - 1);
                    }

                    Image image = this.lvMain.getSelectionModel().getSelectedItem();
                    int index = this.lvMain.getSelectionModel().getSelectedIndex();

                    this.saveFile(new File(image.getPath()), image, index);
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainImageSaveAs.setOnAction(actionEvent -> {
            try {
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    if (this.tblMainImageHistory.getSelectionModel().isEmpty()) {
                        this.tblMainImageHistory.getSelectionModel().select(this.tblMainImageHistory.getItems().size() - 1);
                    }

                    Image image = this.lvMain.getSelectionModel().getSelectedItem();
                    File f = new File(image.getPath());
                    String extension = FilenameUtils.getExtension(image.getPath());
                    int index = this.lvMain.getSelectionModel().getSelectedIndex();

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(resources.getString("main.image.menu.saveAs.dialog"));
                    fileChooser.setInitialFileName(f.getName());
                    fileChooser.setInitialDirectory(new File(f.getParent()));
                    FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Format", extension);
                    fileChooser.getExtensionFilters().add(extensionFilter);
                    fileChooser.setSelectedExtensionFilter(extensionFilter);
                    File file = fileChooser.showSaveDialog(null);

                    if(file != null) {
                        image.setTitle(file.getName());
                        image.setPath(file.getAbsolutePath());
                        image.setId(0);
                        this.saveFile(file, image, index);
                    }
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainImageMove.setOnAction(event -> {
            this.ctxMainImageMove.getItems().clear();
            this.dirRows = SettingsController.getRowsFromSettings();
            for(SettingsController.DirRow dirRow : this.dirRows) {
                MenuItem menuItem = new MenuItem(dirRow.getTitle());
                menuItem.setOnAction(itemEvent -> {
                    try {
                        Image image = this.lvMain.getSelectionModel().getSelectedItem();
                        BufferedImage bufferedImage = this.getEditedImage(false);
                        ImageHelper.save(image.getPath(), dirRow.getPath() + File.separatorChar + new File(image.getPath()).getName(), bufferedImage);
                    } catch (Exception ex) {
                        Dialogs.printException(ex);
                    }
                });
                this.ctxMainImageMove.getItems().add(menuItem);
            }
            this.ctxMainImageMove.show();
        });

        this.ctxMainImageDelete.setOnAction(event -> {
            try {
                String path = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_DELETE_KEY, "");
                String moveToPath = "";
                if(path!=null) {
                    if(!path.trim().isEmpty()) {
                        moveToPath = path.trim();
                    }
                }

                Image image = this.lvMain.getSelectionModel().getSelectedItem();
                if(image!=null) {
                    this.currentImage = null;
                    PhotoManager.GLOBALS.getDatabase().deleteImage(image);
                    this.editController.getPreview().setImage(null);
                    this.ivMainImage.setImage(null);
                    this.histogramController.setImage(null);
                    this.metaDataController.setImage(null);

                    if(moveToPath.isEmpty()) {
                        Files.delete(Paths.get(image.getPath()));
                    } else {
                        Files.move(Paths.get(image.getPath()), Paths.get(moveToPath + File.separatorChar + new File(image.getPath()).getName()));
                    }
                    this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue());
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainImageAssemble.setOnAction(event -> {
            Dialog<Dialogs.AssembleResult> dialog = Dialogs.createAssembleDialog();
            Optional<Dialogs.AssembleResult> result = dialog.showAndWait();
            result.ifPresent(assembleResult -> {
                this.lvMain.getScene().setCursor(Cursor.WAIT);

                Directory directory = this.tvMain.getSelectionModel().getSelectedItem().getValue();
                List<Image> images = this.lvMain.getSelectionModel().getSelectedItems();

                CreateAssembledImage createAssembledImage = new CreateAssembledImage(this.pbMain, this.lblMessages, assembleResult, directory, images);
                createAssembledImage.onFinish(() -> fillImageList(directory));
                new Thread(createAssembledImage).start();
            });
        });

        this.ctxMainImageScale.setOnAction(event -> {
            Dialog<Dialogs.ResizeResult> dialog = Dialogs.createResizeDialog();
            Optional<Dialogs.ResizeResult> result = dialog.showAndWait();
            result.ifPresent(resizeResult -> {
                int width = resizeResult.width;
                int height = resizeResult.height;

                this.lvMain.getScene().setCursor(Cursor.WAIT);

                Directory directory = this.tvMain.getSelectionModel().getSelectedItem().getValue();
                List<Image> images = this.lvMain.getSelectionModel().getSelectedItems();

                ScaleImages scaleImages = new ScaleImages(this.pbMain, this.lblMessages, width, height, directory, images);
                scaleImages.onFinish(()->fillImageList(directory));
                new Thread(scaleImages).start();
            });
        });

        this.slMainImageZoom.valueProperty().addListener((observable, oldValue, newValue) -> this.lblMainImageZoom.setText(newValue.intValue() + " %"));

        this.cmdMainImageZoom.setOnAction(event -> {
            // scale image
            if(!this.lvMain.getSelectionModel().isEmpty() && this.currentImage!=null) {
                Image image = this.lvMain.getSelectionModel().getSelectedItem();
                int width = (int) (image.getWidth() * (this.slMainImageZoom.getValue() / 100.0));
                int height = (int) (image.getHeight() * (this.slMainImageZoom.getValue() / 100.0));
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(this.cache.getOriginal(), null);
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

                    if(!this.txtMainImageName.getText().trim().isEmpty()) {
                        image.setTitle(this.txtMainImageName.getText().trim());
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
                    this.lvMain.getItems().set(this.lvMain.getSelectionModel().getSelectedIndex(), image);
                } else {
                    if(!this.tvMain.getSelectionModel().isEmpty()) {
                        Directory directory = this.tvMain.getSelectionModel().getSelectedItem().getValue();
                        if(!this.cloudController.getTreeView().getSelectionModel().isEmpty()) {
                            Cloud cloud = new Cloud();
                            cloud.setPath(this.cloudController.getWebDav().getBaseUrl() + this.cloudController.getTreeView().getSelectionModel().getSelectedItem().getValue().get().getPath());
                            if (directory.getCloud()!=null) {
                                cloud.setId(directory.getCloud().getId());
                            }
                            directory.setCloud(cloud);
                            this.tvMain.getSelectionModel().getSelectedItem().getValue().setCloud(cloud);
                        }
                        PhotoManager.GLOBALS.getDatabase().updateDirectory(directory);
                    }
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.tblMainImageHistory.getSelectionModel().selectedItemProperty().addListener((observableValue, temporaryEdited, t1) -> {
            if(!this.tblMainImageHistory.getSelectionModel().isEmpty()) {
                this.getEditedImage(true);
            }
        });

        this.cmdMainTemplate.setOnAction(event ->  this.cmdMainTemplateAdd.setVisible(!this.cmdMainTemplateAdd.isVisible()));

        this.cmdMainTemplateAdd.setOnAction(event -> {
            try {
                Template template = new Template();
                template.setTitle(this.cmbMainTemplates.getValue());
                List<Template> templates = PhotoManager.GLOBALS.getDatabase().getTemplates("name='" + this.cmbMainTemplates.getValue() + "'");
                if(!templates.isEmpty()) {
                    template.setId(templates.get(0).getId());
                }
                template.getPreferences().put(Template.Preference.ZOOM.toString(), String.valueOf(this.slMainImageZoom.getValue()));
                template = editController.updateTemplate(template);
                PhotoManager.GLOBALS.getDatabase().insertOrUpdateTemplate(template);
                this.fillTemplates();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.cmdMainTemplateDelete.setOnAction(event -> {
            try {
                PhotoManager.GLOBALS.getDatabase().deleteTemplate(this.cmbMainTemplates.getValue());
                this.fillTemplates();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.cmbMainTemplates.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue!=null) {
                    if(newValue.isEmpty()) {
                        this.slMainImageZoom.setValue(100.0);
                        this.editController.reset();
                    } else {
                        List<Template> templates = PhotoManager.GLOBALS.getDatabase().getTemplates("name='" + newValue + "'");
                        if(!templates.isEmpty()) {
                            Template template = templates.get(0);
                            this.slMainImageZoom.setValue(Double.parseDouble(template.getPreferences().getOrDefault(Template.Preference.ZOOM.toString(), "100.0")));
                            this.editController.setTemplate(template);
                        }
                    }
                    this.cmdMainImageZoom.fire();
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
                        this.lvMain.getSelectionModel().clearSelection();
                        this.lvMain.getItems().clear();
                        this.ivMainImage.setImage(null);
                        this.histogramController.setImage(null);
                        this.metaDataController.setImage(null);
                        this.editController.getPreview().setImage(null);
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

        this.ctxMainSlideshow.setOnAction(event -> {
            if(!this.tvMain.getSelectionModel().isEmpty()) {
                this.slideshowController.getImages(this.lvMain.getItems());
                this.tbpMain.getSelectionModel().select(this.tbSlideshow);
            }
        });

        this.cmdMainImageSearch.setOnAction(event -> {
            if(!this.txtMainImageSearch.isVisible()) {
                this.txtMainImageSearch.setVisible(true);
                this.txtMainImageSearch.setPrefWidth(Region.USE_COMPUTED_SIZE);
            } else {
                if(this.txtMainImageSearch.getText().trim().isEmpty()) {
                    this.txtMainImageSearch.setVisible(false);
                    this.txtMainImageSearch.setPrefWidth(0);
                }
                search();
            }
        });



        this.tvMain.setOnDragOver(mouseEvent -> {
            if(mouseEvent.getGestureSource() != this.tvMain && mouseEvent.getDragboard().hasString()) {
                mouseEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            mouseEvent.consume();
        });

        this.tvMain.setOnDragDropped(mouseEvent -> {
            try {
                Dragboard db = mouseEvent.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String content = db.getString();
                    for(String strIndex : content.split(";")) {
                        if(!strIndex.trim().isEmpty()) {
                            int index = Integer.parseInt(strIndex.trim());
                            Image image = this.unsplashController.getUnsplashListView().getItems().get(index);

                            if(!this.tvMain.getSelectionModel().isEmpty()) {
                                Directory directory = this.tvMain.getSelectionModel().getSelectedItem().getValue();
                                File file = new File(directory.getPath() + File.separatorChar + image.getExtended().getOrDefault("id", "tmp") + ".jpg");
                                InputStream inputStream = new URL(image.getExtended().get("unSplash")).openStream();
                                BufferedImage bufferedImage = ImageIO.read(inputStream);
                                inputStream.close();
                                bufferedImage = ImageHelper.addWaterMark(bufferedImage, "(c) Unsplash");
                                FileUtils.writeByteArrayToFile(file, ImageHelper.imageToByteArray(bufferedImage));
                                image.setPath(file.getAbsolutePath());
                                image.setDirectory(directory);
                                PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);

                                this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue());
                                success = true;
                            } else {
                                success = false;
                            }
                        }
                    }
                }

                mouseEvent.setDropCompleted(success);
                mouseEvent.consume();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.menMainSettings.setOnAction(event -> this.tbpMain.getSelectionModel().select(this.tbSettings));
        this.menMainMap.setOnAction(event -> {
            this.tbpMain.getSelectionModel().select(this.tbMap);
            if(this.tvMain.getSelectionModel().isEmpty()) {
                this.mapController.initMap(null);
            } else {
                this.mapController.initMap(this.tvMain.getSelectionModel().getSelectedItem().getValue());
            }
        });
        this.menMainHelp.setOnAction(event -> this.tbpMain.getSelectionModel().select(this.tbHelp));
        this.menMainClose.setOnAction(event -> Platform.exit());

        this.splPaneDirectories.getDividers().get(0).positionProperty().addListener(obs -> this.saveSplitPanePositions());
        this.splPaneImages.getDividers().get(0).positionProperty().addListener(obs -> this.saveSplitPanePositions());
        this.splPaneImage.getDividers().get(0).positionProperty().addListener(obs -> this.saveSplitPanePositions());
    }

    void back() {
        this.tbpMain.getSelectionModel().select(this.tbMain);
    }

    public ProgressBar getProgressBar() {
        return this.pbMain;
    }

    public Label getMessages() {
        return this.lblMessages;
    }

    void setMessage(String msg) {
        this.lblMessages.setText(msg);
    }

    public Directory getDirectory() {
        return this.directory;
    }

    public MapView getMapView() {
        return this.gvMainServicesLocation;
    }

    public ListView<Image> getLvMain() {
        return this.lvMain;
    }

    public TreeView<Directory> getTvMain() {
        return this.tvMain;
    }

    public void hideBars(boolean hide) {
        this.toolbarMain.setVisible(!hide);
        this.menMain.setVisible(!hide);

        AnchorPane.setTopAnchor(this.tbpMain, hide ? -30.0 : -6.0);
        AnchorPane.setBottomAnchor(this.tbpMain, hide ? 0.0 : 30.0);
    }

    private void initControllers() {
        this.settingsController.init(this);
        this.mapController.init(this);
        this.slideshowController.init(this);
        this.helpController.init(this);

        this.histogramController.init(this);
        this.metaDataController.init(this);
        this.tinifyController.init(this);
        this.unsplashController.init(this);
        this.cloudController.init(this);
        this.editController.init(this);
    }

    public void reloadHistory(long id) throws Exception {
        this.tblMainImageHistory.getItems().clear();
        TemporaryEdited root = new TemporaryEdited();
        root.setChangeType(TemporaryEdited.ChangeType.None);
        this.tblMainImageHistory.getItems().add(root);
        for(TemporaryEdited temporaryEdited : PhotoManager.GLOBALS.getDatabase().getTemporaryEdited(id)) {
            this.tblMainImageHistory.getItems().add(temporaryEdited);
        }
    }

    private void initBindings() {
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.cmdMainFolder.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.txtMainFolderName.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.chkMainRecursive.visibleProperty());

        this.cmdMainTemplateAdd.visibleProperty().bindBidirectional(this.cmdMainTemplateDelete.visibleProperty());
        this.cmdMainTemplateAdd.visibleProperty().bindBidirectional(this.cmbMainTemplates.visibleProperty());

        ControlsHelper.addColumnsToTable(this.tblMainImageHistory, Arrays.asList(
            new String[]{"ChangeType", "main.image.history.type", "changeType"},
            new String[]{"Value", "main.image.history.value", "stringValue"}
        ));

        this.lvMain.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    void initTinify() {
        this.tinifyController.initTinify();
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
        this.lvMain.setCellFactory(param ->  this.initListCell());
        this.unsplashController.getUnsplashListView().setCellFactory(param -> this.initListCell());
        this.unsplashController.getUnsplashListView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private ListCell<Image> initListCell() {
        return new ListCell<>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(Image name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(name.getTitle());
                    javafx.scene.image.Image image = new javafx.scene.image.Image(new ByteArrayInputStream(name.getThumbnail()));
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    if(bufferedImage!=null) {
                        this.imageView.setImage(SwingFXUtils.toFXImage(ImageHelper.scale(bufferedImage, 50, 50), null));
                        setGraphic(this.imageView);
                    }
                }
            }
        };
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
        this.cache.setOriginal(image);
        Image img = this.lvMain.getSelectionModel().getSelectedItem();
        javafx.scene.image.Image preview = new javafx.scene.image.Image(new ByteArrayInputStream(img.getThumbnail()));
        this.cache.setOriginalPreview(SwingFXUtils.fromFXImage(preview, null));
        this.cache.setPreviewImage(ImageHelper.deepCopy(this.cache.getOriginalPreview()));
        this.editController.getPreview().setImage(preview);
        this.ivMainImage.setFitWidth(image.getWidth());
        this.ivMainImage.setFitHeight(image.getHeight());
        this.ivMainImage.setViewport(new Rectangle2D(0, 0, image.getWidth(), image.getHeight()));
        this.ivMainImage.setPreserveRatio(true);
        this.ivMainImage.setImage(image);
        this.slMainImageZoom.setValue(100.0);
        this.editController.getWatermark().setText("");
    }

    public void fillImageList(Directory directory) {
        this.fillImageList(directory, "");
    }

    private void fillImageList(Directory directory, String search) {
        String finalSearch = search.trim();
        try {
            if (directory != null) {
                if(directory instanceof Folder) {
                    Platform.runLater(this.lvMain.getItems()::clear);
                    Folder folder = (Folder) directory;
                    Files.list(Paths.get(folder.getPath())).forEach(path -> {
                        try {
                            File file = path.toFile();
                            if(file.isFile()) {
                                String extension = FilenameUtils.getExtension(file.getAbsolutePath());
                                if(Arrays.asList(ImageHelper.EXTENSIONS).contains(extension)) {
                                    Image image = new Image();
                                    image.setPath(file.getAbsolutePath());
                                    image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(ImageHelper.getImage(file.getAbsolutePath()), 50, 50)));
                                    image.setTitle(file.getName());
                                    Platform.runLater(()->this.lvMain.getItems().add(image));
                                }
                            }
                        } catch (Exception ex) {
                            Dialogs.printException(ex);
                        }
                    });
                } else {
                    Platform.runLater(this.lvMain.getItems()::clear);
                    for (Image image : PhotoManager.GLOBALS.getDatabase().getImages(directory, false)) {
                        if (!new File(image.getPath()).exists()) {
                            PhotoManager.GLOBALS.getDatabase().deleteImage(image);
                            continue;
                        }

                        boolean foundItem = true;
                        if (!finalSearch.isEmpty()) {
                            foundItem = false;
                            if (image.getCategory() != null) {
                                System.out.println(image.getCategory().getTitle());
                                if (image.getCategory().getTitle().trim().toLowerCase().contains(finalSearch)) {
                                    foundItem = true;
                                }
                            }
                            if (!image.getTags().isEmpty()) {
                                for (DescriptionObject descriptionObject : image.getTags()) {
                                    System.out.println(descriptionObject.getTitle());
                                    if (descriptionObject.getTitle().trim().toLowerCase().contains(finalSearch)) {
                                        foundItem = true;
                                    }
                                }
                            }
                        }

                        if (foundItem) {
                            Platform.runLater(()->this.lvMain.getItems().add(image));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void fillCategoryAndTags() {
        if(!this.lvMain.getSelectionModel().isEmpty()) {
            Image image = this.lvMain.getSelectionModel().getSelectedItem();
            if(image!=null) {
                this.pnlMainImage.setText(image.getTitle());
                this.txtMainImageName.setText(image.getTitle());
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

    private void fillTemplates() {
        try {
            this.cmbMainTemplates.getItems().clear();
            PhotoManager.GLOBALS.getDatabase().getTemplates("").forEach(
                template -> this.cmbMainTemplates.getItems().add(template.getTitle())
            );
            this.cmbMainTemplates.getItems().add(0, "");
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private BufferedImage getEditedImage(boolean updateIV) {
        BufferedImage bufferedImage = null;
        long id = this.tblMainImageHistory.getSelectionModel().getSelectedItem().getId();
        int hue = 100, saturation = 100, brightness = 100, rotation = 0, width = 0, height = 0;
        String watermark = "", filter = "";

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

                BufferedImage image = SwingFXUtils.fromFXImage(this.ivMainImage.getImage(), null);
                ImageHelper.changeHSB(image, SwingFXUtils.fromFXImage(this.currentImage, null), hue, saturation, brightness);
                if(!watermark.trim().isEmpty()) {
                    image = ImageHelper.addWaterMark(image, watermark);
                }
                image = ImageHelper.resize(image, this.currentImage, width, height);
                bufferedImage = ImageHelper.rotate(image, rotation);

                ImageHelper.Filter.Type type = this.editController.getFilterTypeBySelectedItem(filter);
                if(type!=null) {
                    bufferedImage = ImageHelper.addFilter(bufferedImage, type);
                }

                javafx.scene.image.Image img = SwingFXUtils.toFXImage(bufferedImage, null);

                if(updateIV) {
                    this.ivMainImage.setImage(img);
                }

                if(id== temp.getId()) {
                    break;
                }
            }
        }
        return bufferedImage;
    }

    private void saveFile(File file, Image image, int index) throws Exception {
        BufferedImage bufferedImage = this.getEditedImage(false);

        ImageHelper.save(image.getPath(), file.getAbsolutePath(), bufferedImage);
        for (int row = 0; row <= this.tblMainImageHistory.getSelectionModel().getSelectedIndex(); row++) {
            TemporaryEdited temporaryEdited = this.tblMainImageHistory.getItems().get(row);
            if (temporaryEdited != null) {
                if (temporaryEdited.getId() != 0) {
                    PhotoManager.GLOBALS.getDatabase().removeHistory(temporaryEdited, image.getId());
                }
            }
        }
        image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(ImageHelper.getImage(image.getPath()), 50, 50)));
        PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
        this.lvMain.getItems().set(index, image);
        this.lvMain.getSelectionModel().clearSelection();
        this.lvMain.getSelectionModel().select(index);
    }

    private void saveSplitPanePositions() {
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_DIRECTORIES, this.splPaneDirectories.getDividerPositions()[0], false);
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_IMAGES, this.splPaneImages.getDividerPositions()[0], false);
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_IMAGE, this.splPaneImage.getDividerPositions()[0], false);
    }

    private void loadSplitPanePositions() {
        this.splPaneDirectories.setDividerPositions(PhotoManager.GLOBALS.getSetting(Globals.POSITION_DIRECTORIES, this.splPaneDirectories.getDividerPositions()[0]));
        this.splPaneImages.setDividerPositions(PhotoManager.GLOBALS.getSetting(Globals.POSITION_IMAGES, this.splPaneImages.getDividerPositions()[0]));
        this.splPaneImage.setDividerPositions(PhotoManager.GLOBALS.getSetting(Globals.POSITION_IMAGE, this.splPaneImage.getDividerPositions()[0]));
    }
}
