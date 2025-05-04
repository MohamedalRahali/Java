package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class UserBarController {
    @FXML
    private void goToProfile(ActionEvent event) throws IOException {
        navigate("/fxml/user_profile.fxml", "Mon Profil");
    }
    @FXML private Button logoutButton;

    @FXML
    private void goToCreateReclamation(ActionEvent event) throws IOException {
        navigate("/fxml/create_reclamation.fxml", "Créer une Réclamation");
    }

    @FXML
    private void goToMesReclamations(ActionEvent event) throws IOException {
        navigate("/fxml/mes_reclamations.fxml", "Mes Réclamations");
    }

    @FXML
    private void goToEvents(ActionEvent event) throws IOException {
        navigate("/fxml/client_events.fxml", "Événements");
    }

    @FXML
    private void goToUserHome(ActionEvent event) throws IOException {
        navigate("/fxml/user_home.fxml", "Accueil Utilisateur");
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        navigate("/fxml/login.fxml", "Login");
    }

    private void navigate(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
    }
}
