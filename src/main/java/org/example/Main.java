package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Démarrage de l'application...");
            
            // Chargement du fichier FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/Home.fxml"));
            
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML non trouvé: /fxml/Home.fxml");
            }
            
            System.out.println("Fichier FXML trouvé à: " + loader.getLocation());
            
            // Chargement de la scène
            Parent root = loader.load();
            System.out.println("Scène chargée avec succès");
            
            // Configuration de la fenêtre principale
            primaryStage.setTitle("Gestion d'Événements");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
            
            System.out.println("Application démarrée avec succès");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
            
            // Affichage d'une alerte d'erreur
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur de démarrage");
            alert.setHeaderText("Impossible de démarrer l'application");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.showAndWait();
            
            // Arrêt de l'application
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}