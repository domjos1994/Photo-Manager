package de.domjos.photo_manager.controller.mainController;

import de.domjos.photo_manager.PhotoManager;
import de.domjos.photo_manager.helper.ImageHelper;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.utils.Dialogs;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;

import java.awt.image.BufferedImage;
import java.util.*;

public class Histogram {
    private BarChart<String, Integer> bcHistogram;
    private CheckBox chkHistogram, chkRed, chkGreen, chkBlue;
    private Image image;

    public Histogram(BarChart<String, Integer> bcHistogram, CheckBox chkHistogram, CheckBox chkRed, CheckBox chkGreen, CheckBox chkBlue) {
        this.bcHistogram = bcHistogram;
        this.chkHistogram = chkHistogram;
        this.chkRed = chkRed;
        this.chkGreen = chkGreen;
        this.chkBlue = chkBlue;
        this.setImage(null);

        this.chkHistogram.selectedProperty().addListener((observable)-> this.fillBarChart());
        this.chkRed.selectedProperty().addListener((observable)-> this.fillBarChart());
        this.chkGreen.selectedProperty().addListener((observable)-> this.fillBarChart());
        this.chkBlue.selectedProperty().addListener((observable)-> this.fillBarChart());
    }

    public void setImage(Image image) {
        this.image = image;
        this.fillBarChart();
    }

    private void fillBarChart() {
        try {
            if(this.image!=null) {
                BufferedImage bufferedImage = ImageHelper.getImage(this.image.getPath());
                this.bcHistogram.getData().clear();

                if(bufferedImage!=null) {
                    ResourceBundle ln = PhotoManager.GLOBALS.getLanguage();

                    Map<Integer, String> typeMap = new LinkedHashMap<>();
                    if(this.chkHistogram.isSelected()) {
                        typeMap.put(0, ln.getString("main.image.histogram"));
                    }
                    if(this.chkRed.isSelected()) {
                        typeMap.put(1, ln.getString("main.image.histogram.red"));
                    }
                    if(this.chkGreen.isSelected()) {
                        typeMap.put(2, ln.getString("main.image.histogram.green"));
                    }
                    if(this.chkBlue.isSelected()) {
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
                        this.bcHistogram.getData().add(series);
                    }
                }
                this.bcHistogram.setLegendVisible(true);
            } else {
                this.chkHistogram.setSelected(true);
                this.chkRed.setSelected(true);
                this.chkBlue.setSelected(true);
                this.chkGreen.setSelected(true);
                this.bcHistogram.getData().clear();
            }
        } catch (Exception ex) {
            Dialogs.printException(ex);
        }
    }
}
