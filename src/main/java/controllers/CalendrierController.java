package controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;

public class CalendrierController {

    @FXML
    private WebView webView;

    @FXML
    public void initialize() {
        try {
            WebEngine webEngine = webView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            // Charger le fichier HTML local
            URL url = getClass().getResource("/html/calendar.html");
            if (url != null) {
                webEngine.load(url.toExternalForm());
                // Ajouter un listener pour diagnostiquer les erreurs de chargement
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.FAILED) {
                        System.err.println("Échec du chargement de calendar.html : " + webEngine.getLoadWorker().getException());
                    }
                });
            } else {
                System.err.println("Erreur : Fichier calendar.html introuvable dans /html/calendar.html");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de WebView : " + e.getMessage());
            e.printStackTrace();
        }
    }
}