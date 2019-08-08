package de.domjos.photo_manager.helper;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.*;
import de.domjos.photo_manager.model.gallery.Image;
import de.domjos.photo_manager.model.gallery.MetaData;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

import static com.lynden.gmapsfx.javascript.object.MapTypeIdEnum.SATELLITE;

public class MapHelper {
    private GoogleMap map;

    public MapHelper(GoogleMapView mapView, LatLong center) {
        MapOptions mapOptions = new MapOptions();
        if(center!=null) {
            mapOptions.center(center);
        }
        mapOptions.mapType(SATELLITE).zoom(12);
        this.map = mapView.createMap(mapOptions);
        mapView.getWebview().setContextMenuEnabled(true);
    }

    public void init(List<Image> images) throws Exception {
        for(Image image : images) {
            MetaData metaData = ImageHelper.readMetaData(image.getPath());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLong(metaData.getLatitude(), metaData.getLongitude()));
            File file = File.createTempFile(image.getPath(), image.getName());
            FileUtils.writeByteArrayToFile(file, image.getThumbnail());
            markerOptions.icon(file.getAbsolutePath());
            markerOptions.title(image.getName());
            this.map.addMarker(new Marker(markerOptions));
        }
    }
}
