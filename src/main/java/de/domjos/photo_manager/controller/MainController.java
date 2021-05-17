package de.domjos.photo_manager.controller;

import com.gluonhq.maps.MapView;
import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.controller.subController.*;
import de.domjos.photo_manager.custom.ZoomImageView;
import de.domjos.photo_manager.helper.ArgsHelper;
import de.domjos.photo_manager.helper.ControlsHelper;
import de.domjos.photo_manager.images.ImageHelper;
import de.domjos.photo_manager.helper.InitializationHelper;
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
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MainController extends ParentController {
    public TitledPane history, unsplash, histogram, metaData, edit, tinify, cloud;
    public AnchorPane settings, map, slideshow;

    private @FXML SplitPane splPaneDirectories, splPaneImages, splPaneImage;

    private @FXML MenuBar menMain;
    private @FXML ToolBar toolbarMain;

    private @FXML TabPane tbpMain;
    private @FXML Tab tbMain, tbSettings, tbBatch, tbMap, tbSlideshow;
    private @FXML MenuItem menMainSettings, menMainBatch, menMainClose, menMainMap, menMainHelp;
    private @FXML MenuItem menMainDatabaseNew, menMainDatabaseDelete;
    private @FXML Menu menMainDatabaseOpen;

    private @FXML ContextMenu ctxMainDirectory;
    private @FXML MenuItem ctxMainDelete, ctxMainRecreate, ctxMainSlideshow, ctxMainBatch;

    private @FXML ContextMenu ctxMainImage;
    private @FXML MenuItem ctxMainImageApply, ctxMainImageSaveAs, ctxMainImageAssemble, ctxMainImageScale, ctxMainUpdateDatabase;
    private @FXML MenuItem ctxMainImageDelete;
    private @FXML Menu ctxMainImageMove;

    private @FXML TreeView<Directory> tvMain;
    private @FXML ListView<Image> lvMain;
    private @FXML ZoomImageView zivMainImage;

    private @FXML Button cmdMainAddFolder, cmdMainReload, cmdMainBatchStart, cmdMainFolder, cmdMainFolderSave, cmdMainImageSave;
    private @FXML CheckBox chkMainRecursive;
    private @FXML TextField txtMainFolderName, tmpPath;
    private @FXML TextField txtMainImageCategory, txtMainImageTags, txtMainImageName;

    private @FXML Button cmdMainImageSearch;
    private @FXML TextField txtMainImageSearch;

    private @FXML Button cmdMainTemplate, cmdMainTemplateAdd, cmdMainTemplateDelete;
    private @FXML ComboBox<String> cmbMainTemplates;

    private @FXML MapView gvMainServicesLocation;

    private @FXML TitledPane pnlMainImage;

    private @FXML Label lblMessages;
    private @FXML ProgressBar pbMain;

    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML SettingsController settingsController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML BatchController batchController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML MapController mapController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML SlideshowController slideshowController;

    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML HistogramController histogramController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML MetaDataController metaDataController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML TinifyController tinifyController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML UnsplashController unsplashController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML InstagramController instagramController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML CloudController cloudController;
    @SuppressWarnings({"UnusedDeclaration"})
    private @FXML EditController editController;
    public @FXML HistoryController historyController;

    private long rootID;
    private Directory directory;
    private javafx.scene.image.Image currentImage;
    private final Cache cache = new Cache();
    private SaveFolderTask importTask;

    @Override
    public void initialize(ResourceBundle resources) {
        ControlsHelper.initController(Arrays.asList(settingsController, batchController, mapController, slideshowController,
            histogramController, metaDataController, tinifyController, unsplashController, instagramController,
            cloudController, editController, historyController), this);
        this.zivMainImage.setController(this);
        this.initBindings();
        this.initTinify();
        this.initTreeView();
        this.initListView();
        this.fillTemplates();
        this.loadSplitPanePositions();
        this.reloadOnStart();


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
        PhotoManager.GLOBALS.getCloseRunnable().add(this::saveSplitPanePositions);

        this.cmdMainReload.setOnAction(event -> this.initTreeView());

        this.cmdMainAddFolder.setOnAction(event -> this.enableFolderControls());
        this.cmdMainFolder.setOnAction(event -> {
            File path = Dialogs.printDirectoryChooser(resources.getString("main.dir.path"));
            if(path!=null) {
                this.tmpPath.setText(path.getAbsolutePath());
                this.cmdMainFolder.setTooltip(new Tooltip(path.getAbsolutePath()));
            }
        });
        this.cmdMainFolderSave.setOnAction(event -> {
            try {
                directory.setPath(this.tmpPath.getText());
                directory.setTitle(txtMainFolderName.getText());
                final long[] parentId = {rootID};
                if(!tvMain.getSelectionModel().isEmpty()) {
                    parentId[0] = tvMain.getSelectionModel().getSelectedItem().getValue().getId();
                }
                boolean recursive = chkMainRecursive.isSelected();
                directory.setRecursive(recursive);
                directory.setLibrary(true);

                importTask = new SaveFolderTask(this.pbMain, this.lblMessages, this.directory, parentId[0], recursive);
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
                    this.cmdMainBatchStart.setDisable(true);
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
                    ControlsHelper.unbind(this.lblMessages, this.pbMain);
                    this.lblMessages.setText(String.format(resources.getString("main.image.selected"), this.lvMain.getSelectionModel().getSelectedItems().size(), this.lvMain.getItems().size()));

                    File img = new File(newValue.getPath());
                    this.loadImage(img);

                    this.histogramController.setImage(newValue);
                    this.metaDataController.setImage(newValue);
                    this.historyController.reloadHistory(newValue.getId());
                    this.fillCategoryAndTags();
                    this.cloudController.fillCloudWithDefault();
                    this.cloudController.getCloudPath();
                    this.editController.setCache(this.cache);

                    this.historyController.selectLast();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainDirectory.setOnShowing(event -> {
            this.ctxMainSlideshow.setDisable((this.tvMain.getSelectionModel().isEmpty() || this.lvMain.getItems().isEmpty()));
            this.ctxMainBatch.setDisable((this.tvMain.getSelectionModel().isEmpty() || this.lvMain.getItems().isEmpty()));
        });

        this.ctxMainImage.setOnShowing(event -> {
            this.ctxMainImageAssemble.setDisable(this.lvMain.getSelectionModel().getSelectedItems().size()<=1);
            this.ctxMainImageScale.setDisable(this.lvMain.getSelectionModel().getSelectedItems().size()<=1);
            this.ctxMainUpdateDatabase.setDisable(this.lvMain.getSelectionModel().isEmpty());
        });

        this.ctxMainImageApply.setOnAction(event -> {
            try {
                if(!this.lvMain.getSelectionModel().isEmpty()) {
                    this.historyController.selectLast();

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
                    this.historyController.selectLast();

                    Image image = this.lvMain.getSelectionModel().getSelectedItem();
                    String extension = FilenameUtils.getExtension(image.getPath());
                    int index = this.lvMain.getSelectionModel().getSelectedIndex();
                    String title = resources.getString("main.image.menu.saveAs.dialog");

                    File file = Dialogs.printSaveFileChooser(title, Collections.singletonList(extension + ":" + extension));
                    if(file != null) {
                        image.setTitle(file.getName());
                        //image.setPath(file.getAbsolutePath());
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

            try {
                List<Directory> directories = PhotoManager.GLOBALS.getDatabase().getDirectories("folder<>0", false);
                for(Directory directory : directories) {
                    MenuItem menuItem = new MenuItem(directory.getTitle());
                    menuItem.setOnAction(itemEvent -> {
                        try {
                            for(Image image : this.lvMain.getSelectionModel().getSelectedItems()) {
                                HistoryTask historyTask = new HistoryTask(this.pbMain, this.lblMessages, this.historyController.getSelectedId(), this.historyController.getItems(), this.zivMainImage.getImage(), currentImage);
                                historyTask.onFinish(() -> {
                                    try {
                                        BufferedImage bufferedImage = historyTask.getValue();
                                        ImageHelper.save(image.getPath(), directory.getPath() + File.separatorChar + new File(image.getPath()).getName(), bufferedImage);

                                        Dialogs.printNotification(Alert.AlertType.INFORMATION, this.lang.getString("main.image.menu.copy.success"), this.lang.getString("main.image.menu.copy.success"));
                                    } catch (Exception ex) {
                                        Dialogs.printException(ex);
                                    }
                                });
                                new Thread(historyTask).start();
                            }
                        } catch (Exception ex) {
                            Dialogs.printException(ex);
                        }
                    });
                    this.ctxMainImageMove.getItems().add(menuItem);
                }
                this.ctxMainImageMove.show();
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.ctxMainImageDelete.setOnAction(event -> {
            try {
                String path = PhotoManager.GLOBALS.getSetting(Globals.DIRECTORIES_DELETE_KEY, "");
                String moveToPath = "";
                if(path!=null) {
                    if(!path.trim().isEmpty() && new File(path).exists()) {
                        moveToPath = path.trim();
                    }
                }

                for(Image image : this.lvMain.getSelectionModel().getSelectedItems()) {
                    if(image!=null) {
                        this.currentImage = null;
                        PhotoManager.GLOBALS.getDatabase().deleteImage(image);
                        this.editController.getPreview().setImage(null);
                        this.zivMainImage.resetImage();
                        this.histogramController.setImage(null);
                        this.metaDataController.setImage(null);

                        if(moveToPath.isEmpty()) {
                            Files.delete(Paths.get(image.getPath()));
                        } else {
                            Files.move(Paths.get(image.getPath()), Paths.get(moveToPath + File.separatorChar + new File(image.getPath()).getName()));
                        }
                    }
                }
                this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue());
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

        this.ctxMainUpdateDatabase.setOnAction(event -> {
            try {
                if(!this.tvMain.getSelectionModel().isEmpty()) {
                    String msg = resources.getString("main.image.import");
                    this.directory = this.tvMain.getSelectionModel().getSelectedItem().getValue();

                    ReloadTask reloadTask = new ReloadTask(this.pbMain, this.lblMessages, msg, this.directory.getId());
                    new Thread(reloadTask).start();
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
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
                        if(PhotoManager.GLOBALS.getSetting(Globals.SAVE_META, false)) {
                            ImageHelper.changeExifMetadata(image.getPath(), TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, image.getTitle());
                        }
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
                        PhotoManager.GLOBALS.getDatabase().insertOrUpdateDirectory(directory, -1, true);
                    }
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
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
                template.getPreferences().put(Template.Preference.ZOOM.toString(), String.valueOf(this.zivMainImage.getZoomValue()));
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
                        this.zivMainImage.setZoomValue(100.0);
                        this.editController.reset();
                    } else {
                        List<Template> templates = PhotoManager.GLOBALS.getDatabase().getTemplates("name='" + newValue + "'");
                        if(!templates.isEmpty()) {
                            Template template = templates.get(0);
                            this.zivMainImage.setZoomValue(Double.parseDouble(template.getPreferences().getOrDefault(Template.Preference.ZOOM.toString(), "100.0")));
                            this.editController.setTemplate(template);
                        }
                    }
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
                        this.zivMainImage.resetImage();
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
            if(!this.tvMain.getSelectionModel().isEmpty() && !this.lvMain.getItems().isEmpty()) {
                this.slideshowController.getImages(this.lvMain.getItems());
                this.tbpMain.getSelectionModel().select(this.tbSlideshow);
            }
        });

        this.ctxMainBatch.setOnAction(event -> {
            if(!this.tvMain.getSelectionModel().isEmpty()) {
                this.openBatchWindow();
            }
        });

        this.txtMainImageSearch.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER) {
                this.cmdMainImageSearch.fire();
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
                    this.txtMainImageSearch.setMinWidth(0);
                }
                search();
            }
        });

        this.lvMain.setOnDragDetected(event -> {
            if(!this.lvMain.getSelectionModel().isEmpty()) {
                Dragboard db = this.lvMain.startDragAndDrop(TransferMode.COPY);

                ClipboardContent content = new ClipboardContent();
                List<File> files = new LinkedList<>();
                for(Image image : this.lvMain.getSelectionModel().getSelectedItems()) {
                    files.add(new File(image.getPath()));
                }
                content.putFiles(files);
                db.setContent(content);
                event.consume();
            }
        });

        this.tvMain.setOnDragOver(mouseEvent -> {
            if(mouseEvent.getGestureSource() != this.tvMain && mouseEvent.getDragboard().hasString()) {
                if(!this.tvMain.getSelectionModel().isEmpty()) {
                    mouseEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                } else {
                    mouseEvent.acceptTransferModes(TransferMode.NONE);
                }
            }
            mouseEvent.consume();
        });

        this.tvMain.setOnDragDropped(mouseEvent -> {
            try {
                Dragboard db = mouseEvent.getDragboard();
                if (db.hasString()) {
                    String content = db.getString();
                    if (!this.tvMain.getSelectionModel().isEmpty()) {
                        DragDropTask dragDropTask = new DragDropTask(this.pbMain, this.lblMessages, content, this.tvMain.getSelectionModel().getSelectedItem().getValue(), unsplashController.getUnsplashListView());
                        dragDropTask.onFinish(() -> {
                            this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue());
                            mouseEvent.setDropCompleted(true);
                            mouseEvent.consume();
                        });
                        dragDropTask.onFailed(() -> {
                            mouseEvent.setDropCompleted(false);
                            mouseEvent.consume();
                        });
                        new Thread(dragDropTask).start();
                    } else {
                        mouseEvent.setDropCompleted(false);
                        mouseEvent.consume();
                    }
                }
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });

        this.cmdMainBatchStart.setOnAction(event -> {
            if(!this.tvMain.getSelectionModel().isEmpty()) {
                TreeItem<Directory> treeItem = this.tvMain.getSelectionModel().getSelectedItem();
                if(treeItem != null) {
                    Directory directory = treeItem.getValue();
                    if(directory != null) {
                        if(directory.getFolder() != null) {
                            if(directory.getFolder().getBatchTemplate() != null) {
                                BatchTemplate batchTemplate = directory.getFolder().getBatchTemplate();
                                List<Image> images;
                                if(this.lvMain.getSelectionModel().isEmpty()) {
                                    images = this.lvMain.getItems();
                                } else {
                                    images = this.lvMain.getSelectionModel().getSelectedItems();
                                }
                                boolean state = Dialogs.printConfirmDialog(
                                        Alert.AlertType.WARNING,
                                        this.lang.getString("main.menu.program.batch"),
                                        this.lang.getString("main.menu.program.batch"),
                                        String.format(this.lang.getString("batch.msg.sure"), images.size()));

                                if(state) {
                                    BatchTask batchTask = new BatchTask(this.pbMain, this.lblMessages, batchTemplate, images);
                                    batchTask.onFinish(()-> {
                                        String title = PhotoManager.GLOBALS.getLanguage().getString("batch.msg.finish");
                                        String content = PhotoManager.GLOBALS.getLanguage().getString("batch.msg.finish.content");

                                        Dialogs.printNotification(Alert.AlertType.CONFIRMATION, title, content);
                                        this.fillImageList(this.tvMain.getSelectionModel().getSelectedItem().getValue());
                                    });
                                    new Thread(batchTask).start();
                                }
                            }

                        }
                    }
                }
            }
        });

        this.menMainSettings.setOnAction(event -> {
            this.settingsController.fixColumns(Arrays.asList(1, 2, 2, 2, 1));
            this.tbpMain.getSelectionModel().select(this.tbSettings);
        });
        this.menMainBatch.setOnAction(event -> this.openBatchWindow());
        this.menMainDatabaseNew.setOnAction(event -> {
            try {
                this.addPathToHistory();
                PhotoManager.GLOBALS.saveSetting(Globals.PATH, "", false);
                new PhotoManager().start(PhotoManager.GLOBALS.getStage());
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
        this.menMainDatabaseOpen.setOnAction(event -> {
            this.menMainDatabaseOpen.getItems().clear();
            String[] history = PhotoManager.GLOBALS.getSetting(Globals.OLD_PATHS, "").split("\\|");
            for(String item : history) {
                if(!item.trim().isEmpty()) {
                    MenuItem menuItem = new MenuItem(item.trim());
                    menuItem.setOnAction(subEvent -> {
                        try {
                            this.addPathToHistory();
                            PhotoManager.GLOBALS.saveSetting(Globals.PATH, item.trim(), false);
                            new PhotoManager().start(PhotoManager.GLOBALS.getStage());
                        } catch (Exception ex) {
                            Dialogs.printException(ex);
                        }
                    });
                    this.menMainDatabaseOpen.getItems().add(menuItem);
                }
            }
        });
        this.menMainDatabaseDelete.setOnAction(event -> {
            try {
                PhotoManager.GLOBALS.getDatabase().close();
                String path = PhotoManager.GLOBALS.getSetting(Globals.PATH, "");
                String pathWithHidden = path + File.separatorChar + InitializationHelper.HIDDEN_PROJECT_DIR;
                FileUtils.deleteDirectory(new File(pathWithHidden));
                String history = PhotoManager.GLOBALS.getSetting(Globals.OLD_PATHS, "");
                history = history.replace("|" + path, "");
                PhotoManager.GLOBALS.saveSetting(Globals.OLD_PATHS, history, false);
                PhotoManager.GLOBALS.saveSetting(Globals.PATH, "", false);
                new PhotoManager().start(PhotoManager.GLOBALS.getStage());
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
        this.menMainMap.setOnAction(event -> {
            this.tbpMain.getSelectionModel().select(this.tbMap);
            if(this.tvMain.getSelectionModel().isEmpty()) {
                this.mapController.initMap(null);
            } else {
                this.mapController.initMap(this.tvMain.getSelectionModel().getSelectedItem().getValue());
            }
        });
        this.menMainHelp.setOnAction(event -> {
            String msg = PhotoManager.GLOBALS.getHelp().getString("help");
            String title = PhotoManager.GLOBALS.getLanguage().getString("help.title");

            Dialogs.printNotification(Alert.AlertType.INFORMATION, title, msg);
        });
        this.menMainClose.setOnAction(event -> Platform.exit());
    }

    @Override
    protected void initContextHelp() {
        super.addContextHelp(this.txtMainFolderName, "main.import.name");
        super.addContextHelp(this.chkMainRecursive, "main.import.recursive");
    }

    void back() {
        this.tbpMain.getSelectionModel().select(this.tbMain);
        this.instagramController.hide();
    }

    public ProgressBar getProgressBar() {
        return this.pbMain;
    }

    public Label getMessages() {
        return this.lblMessages;
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

    private void initBindings() {
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.cmdMainFolder.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.txtMainFolderName.visibleProperty());
        this.cmdMainFolderSave.visibleProperty().bindBidirectional(this.chkMainRecursive.visibleProperty());

        this.cmdMainTemplateAdd.visibleProperty().bindBidirectional(this.cmdMainTemplateDelete.visibleProperty());
        this.cmdMainTemplateAdd.visibleProperty().bindBidirectional(this.cmbMainTemplates.visibleProperty());

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
                    this.rootID = PhotoManager.GLOBALS.getDatabase().getDirectories("isRoot=1", true).get(0).getId();
                    TreeItem<Directory> root = treeViewTask.get();
                    this.tvMain.setRoot(root);
                } catch (Exception ex) {
                    Dialogs.printException(ex);
                } finally {
                    this.readArguments();
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
        this.batchController.getBatchSelectedImages().setCellFactory(param -> this.initListCell());
        this.batchController.getBatchSelectedImages().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.instagramController.getInstagramListView().setCellFactory(param -> this.initListCell());
        this.instagramController.getInstagramListView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void UpdateImageView() {
        this.zivMainImage.updateMax();
    }

    private ListCell<Image> initListCell() {
        return new ListCell<>() {
            private final ImageView imageView = new ImageView();
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
            this.chkMainRecursive.setSelected(false);
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
        this.zivMainImage.setImage(image);
        this.zivMainImage.setZoomValue(100.0);
        this.editController.getWatermark().setText("");

        this.loadSplitPanePositions();
    }

    public void fillImageList(Directory directory) {
        this.fillImageList(directory, "");
    }

    private void fillImageList(Directory directory, String search) {
        try {
            this.lvMain.getItems().clear();
            ListViewTask listViewTask = new ListViewTask(this.pbMain, this.lblMessages, directory, search);
            listViewTask.onFinish(() -> {
                try {
                    listViewTask.get().forEach(this.lvMain.getItems()::add);
                } catch (Exception ex) {
                    Dialogs.printException(ex);
                }
            });
            new Thread(listViewTask).start();
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

    public void getEditedImage() {
        long id = this.historyController.getSelectedId();
        HistoryTask historyTask = new HistoryTask(this.pbMain, this.lblMessages, id, this.historyController.getItems(), this.zivMainImage.getImage(), this.currentImage);
        historyTask.onFinish(()-> {
            BufferedImage bufferedImage = historyTask.getValue();
            if(bufferedImage!=null) {
                Platform.runLater(()->zivMainImage.setImage(bufferedImage));
            }
        });
        new Thread(historyTask).start();
    }

    private void saveFile(File file, Image image, int index) {
        HistoryTask historyTask = new HistoryTask(this.pbMain, this.lblMessages, this.historyController.getSelectedId(), this.historyController.getItems(), this.zivMainImage.getImage(), currentImage);
        historyTask.onFinish(() -> {
            try {
                BufferedImage bufferedImage = historyTask.getValue();

                ImageHelper.save(image.getPath(), file.getAbsolutePath(), bufferedImage);
                for (int row = 0; row <= this.historyController.getSelectedIndex(); row++) {
                    TemporaryEdited temporaryEdited = this.historyController.getItems().get(row);
                    if (temporaryEdited != null) {
                        if (temporaryEdited.getId() != 0) {
                            PhotoManager.GLOBALS.getDatabase().removeHistory(temporaryEdited, image.getId());
                        }
                    }
                }
                image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(ImageHelper.getImage(image.getPath()), 50, 50)));
                image.setPath(file.getAbsolutePath());
                PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
                this.lvMain.getItems().set(index, image);
                this.lvMain.getSelectionModel().clearSelection();
                this.lvMain.getSelectionModel().select(index);
            } catch (Exception ex) {
                Dialogs.printException(ex);
            }
        });
        new Thread(historyTask).start();
    }

    private void saveSplitPanePositions() {
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_DIRECTORIES, this.splPaneDirectories.getDividers().get(0).getPosition(), false);
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_IMAGES, this.splPaneImages.getDividers().get(0).getPosition(), false);
        PhotoManager.GLOBALS.saveSetting(Globals.POSITION_IMAGE, this.splPaneImage.getDividers().get(0).getPosition(), false);
    }

    private void loadSplitPanePositions() {
        double divDir = PhotoManager.GLOBALS.getSetting(Globals.POSITION_DIRECTORIES, this.splPaneDirectories.getDividers().get(0).getPosition());
        double divIMGs = PhotoManager.GLOBALS.getSetting(Globals.POSITION_IMAGES, this.splPaneImages.getDividers().get(0).getPosition());
        double divIMG = PhotoManager.GLOBALS.getSetting(Globals.POSITION_IMAGE, this.splPaneImage.getDividers().get(0).getPosition());

        this.changeSize(this.splPaneDirectories, divDir);
        this.changeSize(this.splPaneImages, divIMGs);
        this.changeSize(this.splPaneImage, divIMG);
    }

    private void changeSize(SplitPane spl, double size) {
        Platform.runLater(() -> spl.setDividerPositions(size));
    }

    private void addPathToHistory() {
        String path = PhotoManager.GLOBALS.getSetting(Globals.PATH, "");
        String old_paths = PhotoManager.GLOBALS.getSetting(Globals.OLD_PATHS, "");
        String[] paths = old_paths.split("\\|");
        boolean contains = false;
        for(String old_path : paths) {
            if(old_path.trim().equals(path)) {
                contains = true;
                break;
            }
        }
        if(!contains) {
            PhotoManager.GLOBALS.saveSetting(Globals.OLD_PATHS, old_paths + "|" + path, false);
        }
    }

    private void loadImage(File img) throws Exception {
        javafx.scene.image.Image image = new javafx.scene.image.Image(img.toURI().toURL().toString());
        this.pbMain.progressProperty().bind(image.progressProperty());
        this.fillImage(image);
    }

    private void openBatchWindow() {
        List<Image> images;
        if(this.lvMain.getSelectionModel().isEmpty()) {
            images = this.lvMain.getItems();
        } else {
            images = this.lvMain.getSelectionModel().getSelectedItems();
        }
        this.openBatchWindow(images);
    }

    public void openBatchWindow(List<Image> images) {
        try {
            this.batchController.getBatchTargetFolder().setRoot(null);
            this.batchController.reloadBatchTemplates();
            TreeViewTask treeViewTask = new TreeViewTask(this.pbMain, this.lblMessages);
            treeViewTask.onFinish(()->{
                try {
                    this.batchController.getBatchTargetFolder().setRoot(treeViewTask.get());
                } catch (Exception ex) {
                    Dialogs.printException(ex);
                }
            });
            new Thread(treeViewTask).start();
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }

        this.batchController.addImages(images);
        this.tbpMain.getSelectionModel().select(this.tbBatch);
    }

    public Directory findDirectory(TreeItem<Directory> directory, String search) {
        if(directory != null) {
            if(directory.getValue().getTitle().trim().toLowerCase().contains(search)) {
                return directory.getValue();
            }
            if(directory.getChildren() != null) {
                for(TreeItem<Directory> tmp : directory.getChildren()) {
                    return this.findDirectory(tmp, search);
                }
            }
        }
        return null;
    }

    private void readArguments() {
        String[] arguments = PhotoManager.GLOBALS.getArguments();
        if(arguments != null) {
            if(arguments.length != 0) {
                new ArgsHelper(arguments, this);
            }
        }
        PhotoManager.GLOBALS.setArguments(null);
    }

    public void saveImage(File imageFile, Directory directory) {
        try {
            BufferedImage bufferedImage = ImageHelper.getImage(imageFile.getAbsolutePath());
            if (bufferedImage != null) {
                Image image = new Image();
                image.setHeight(bufferedImage.getHeight());
                image.setWidth(bufferedImage.getWidth());
                image.setPath(imageFile.getAbsolutePath());
                image.setTitle(imageFile.getName().split("\\.")[0].trim());
                image.setThumbnail(ImageHelper.imageToByteArray(ImageHelper.scale(bufferedImage, 128, 128)));
                image.setDirectory(directory);
                PhotoManager.GLOBALS.getDatabase().insertOrUpdateImage(image);
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }

    private void reloadOnStart() {
        if(PhotoManager.GLOBALS.getSetting(Globals.RELOAD_ON_START, false)) {
            ReloadTask reloadTask = new ReloadTask(this.pbMain, this.lblMessages, PhotoManager.GLOBALS.getLanguage().getString("main.image.import"), 1);
            new Thread(reloadTask).start();
        }
    }
}
