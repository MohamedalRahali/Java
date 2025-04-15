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

public class BlogManagementController {

    @FXML
    private void goToCreateBlog(ActionEvent event) {
        loadScene(event, "/fxml/create_blog.fxml", "Créer un Blog");
    }

    @FXML
    private void goToModifyBlog(ActionEvent event) {
        loadScene(event, "/fxml/modify_blog.fxml", "Modifier un Blog");
    }

    @FXML
    private void goToDeleteBlog() {
        System.out.println("Suppression de blog");
        // Implémentez la logique de suppression ici
    }

    @FXML
    private void goBackToHome(ActionEvent event) {
        loadScene(event, "/fxml/home.fxml", "Accueil");
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            URL fxmlURL = getClass().getResource(fxmlPath);
            if (fxmlURL == null) {
                throw new IOException("Fichier FXML introuvable : " + fxmlPath);
            }

            Parent root = FXMLLoader.load(fxmlURL);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue : " + e.getMessage());
            e.printStackTrace();
            // Vous pourriez ajouter une alerte à l'utilisateur ici
        }
    }

    // Méthode optionnelle pour afficher une alerte
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}