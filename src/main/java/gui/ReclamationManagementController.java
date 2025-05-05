package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

/**
 * Controller for managing reclamations (complaints).
 * This class handles the navigation between different reclamation-related views
 * and provides functionality for creating, modifying, and deleting reclamations.
 */
public class ReclamationManagementController {

    @FXML
    private void goToCreateReclamation(ActionEvent event) {
        loadScene(event, "/fxml/create_reclamation.fxml", "Créer une Réclamation");
    }

    @FXML
    private void goToModifyReclamation(ActionEvent event) {
        loadScene(event, "/fxml/modification_reclamation.fxml", "Modifier une Réclamation");
    }

    @FXML
    private void goToDeleteReclamation() {
        System.out.println("Suppression de réclamation");
    }

    @FXML
    private void goBackToHome(ActionEvent event) {
        loadScene(event, "/fxml/Home.fxml", "Accueil");
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            handleSceneLoading(event, fxmlPath, title);
        } catch (IOException e) {
            handleError("Erreur de chargement", "Impossible de charger la page " + title, e);
        }
    }

    /**
     * Handles scene loading with proper error management
     * @param event The action event that triggered the scene change
     * @param fxmlPath Path to the FXML file to load
     * @param title Title of the new window
     * @throws IOException If the FXML file cannot be loaded
     */
    private void handleSceneLoading(ActionEvent event, String fxmlPath, String title) throws IOException {
            URL fxmlURL = getClass().getResource(fxmlPath);
            if (fxmlURL == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
        }

        Parent root = FXMLLoader.load(fxmlURL);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     * @param title Titre de l'erreur
     * @param content Description de l'erreur
     * @param e Exception à logger
     */
    private void handleError(String title, String content, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
