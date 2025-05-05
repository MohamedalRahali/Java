package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

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
            URL fxmlURL = getClass().getResource(fxmlPath);
            if (fxmlURL == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            Parent root = FXMLLoader.load(fxmlURL);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la scène : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
