package controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class CarteController {

    @FXML
    private WebView webView;

    @FXML
    private void initialize() {
        // Load the HTML file in WebView
        String htmlPath = getClass().getResource("/html/MapDisplay.html").toExternalForm();
        if (htmlPath == null) {
            System.err.println("HTML file not found at /html/MapDisplay.html");
            return;
        }
        // Set user agent to enable geolocation
        webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        // Log JavaScript alerts for debugging
        webView.getEngine().setOnAlert(event -> System.out.println("WebView Alert: " + event.getData()));
        webView.getEngine().load(htmlPath);
    }

}