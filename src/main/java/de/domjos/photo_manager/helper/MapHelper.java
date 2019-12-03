package de.domjos.photo_manager.helper;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.gallery.MetaData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Pair;

import java.io.ByteArrayInputStream;
import java.util.List;

public class MapHelper {
    private MapView mapView;

    public MapHelper(MapView mapView, MapPoint center) {
        if(center!=null) {
            mapView.setCenter(center);
        }
        this.mapView = mapView;
    }

    public void init(List<Image> images) throws Exception {
        PoiLayer poiLayer = new PoiLayer();

        for(Image image : images) {
            MetaData metaData = ImageHelper.readMetaData(image.getPath());
            MapPoint mapPoint = new MapPoint(metaData.getLatitude(), metaData.getLongitude());
            ImageView imageView = new ImageView();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(image.getThumbnail());
            imageView.setImage(new javafx.scene.image.Image(byteArrayInputStream));
            byteArrayInputStream.close();
            poiLayer.addPoint(mapPoint, imageView);
        }
        this.mapView.addLayer(poiLayer);
    }

    public static class PoiLayer extends MapLayer {
        private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();

        public PoiLayer() {}

        public void addPoint(MapPoint p, Node icon) {
            this.points.add(new Pair<>(p, icon));
            this.getChildren().add(icon);
            this.markDirty();
        }

        @Override
        protected void layoutLayer() {
            for (Pair<MapPoint, Node> candidate : this.points) {
                MapPoint point = candidate.getKey();
                Node icon = candidate.getValue();
                Point2D mapPoint = getMapPoint(point.getLatitude(), point.getLongitude());
                icon.setVisible(true);
                icon.setTranslateX(mapPoint.getX());
                icon.setTranslateY(mapPoint.getY());
            }
        }
    }
}
