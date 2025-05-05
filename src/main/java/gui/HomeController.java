package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class HomeController {

    private static final Logger logger = Logger.getLogger(HomeController.class.getName());

    @FXML
    private void goToReclamationView(ActionEvent event) {
        try {
            loadScene(event, "/fxml/reclamation_view.fxml", "Liste des Réclamations");
        } catch (Exception e) {
            handleError("Erreur de navigation", "Impossible d'accéder à la vue des réclamations", e);
        }
    }

    @FXML
    private void handleClientSpace(ActionEvent event) {
        try {
            loadScene(event, "/fxml/create_reclamation.fxml", "Créer une Réclamation");
        } catch (Exception e) {
            handleError("Erreur de navigation", "Impossible d'accéder au formulaire de création", e);
        }
    }

    @FXML
    private void handleTypeReclamation(ActionEvent event) {
        try {
            loadScene(event, "/fxml/type_reclamation.fxml", "Types de Réclamation");
        } catch (Exception e) {
            handleError("Erreur de navigation", "Impossible d'accéder à la gestion des types de réclamation", e);
        }
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            URL fxmlURL = getClass().getResource(fxmlPath);
            if (fxmlURL == null) {
                throw new IOException("Fichier FXML non trouvé: " + fxmlPath);
            }

            Parent root = FXMLLoader.load(fxmlURL);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            handleError("Erreur de chargement", "Impossible de charger la scène: " + fxmlPath, e);
        }
    }

    private void handleError(String title, String content, Exception e) {
        logger.severe("Erreur: " + content + " - " + e.getMessage());
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}