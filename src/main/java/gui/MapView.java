package gui;

import javafx.beans.property.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class MapView extends StackPane {
    private final WebView webView;
    private final WebEngine webEngine;
    private final DoubleProperty latitude = new SimpleDoubleProperty(36.8065);
    private final DoubleProperty longitude = new SimpleDoubleProperty(10.1815);
    private final BooleanProperty mapReady = new SimpleBooleanProperty(false);
    private final StringProperty mapType = new SimpleStringProperty("OpenStreetMap");

    public MapView() {
        webView = new WebView();
        webEngine = webView.getEngine();
        getChildren().add(webView);
        
        // Load the map HTML file
        String mapUrl = getClass().getResource("/map.html").toExternalForm();
        webEngine.load(mapUrl);

        // Add JavaScript bridge
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case SUCCEEDED:
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", new JavaBridge());
                    break;
                case FAILED:
                    System.err.println("Failed to load map");
                    break;
                case SCHEDULED:
                case RUNNING:
                    // En cours de chargement
                    break;
                case CANCELLED:
                    System.err.println("Map loading cancelled");
                    break;
                case READY:
                    // Prêt à charger
                    break;
            }
        });
    }

    public void setLocation(double lat, double lon) {
        if (mapReady.get()) {
            webEngine.executeScript(String.format("setMapView(%f, %f)", lat, lon));
            latitude.set(lat);
            longitude.set(lon);
        }
    }

    public void setMapType(String type) {
        if (mapReady.get()) {
            webEngine.executeScript(String.format("setMapType('%s')", type));
            mapType.set(type);
        }
    }

    public void setZoom(int level) {
        if (mapReady.get()) {
            webEngine.executeScript(String.format("setZoom(%d)", level));
        }
    }

    public void scheduleResize() {
        if (mapReady.get()) {
            webEngine.executeScript("forceRedraw()");
        }
    }

    public void setGeocoding(boolean inProgress) {
        // Cette méthode peut être utilisée pour montrer/cacher un indicateur de chargement
    }

    public DoubleProperty latitudeProperty() {
        return latitude;
    }

    public DoubleProperty longitudeProperty() {
        return longitude;
    }

    public BooleanProperty mapReadyProperty() {
        return mapReady;
    }

    public StringProperty mapTypeProperty() {
        return mapType;
    }

    public class JavaBridge {
        public void onMapReady() {
            mapReady.set(true);
            setLocation(latitude.get(), longitude.get());
            setMapType(mapType.get());
        }

        public void onLocationChanged(double lat, double lon) {
            latitude.set(lat);
            longitude.set(lon);
        }
    }
}
