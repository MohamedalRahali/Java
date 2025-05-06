package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class AdminBarController {
    @FXML private Button logoutButton;
    @FXML private Label userEmailLabel;

    // This should be set by the parent controller after loading
    public void setUserEmail(String email) {
        userEmailLabel.setText("Logged in as: " + email);
    }

    @FXML
    private void goToAdminHome() throws IOException {
        navigate("/fxml/admin_home.fxml", "Admin Home");
    }

    @FXML
    private void goToHomeReclamation() throws IOException {
        navigate("/fxml/HomeReclamation.fxml", "Reclamation Home");
    }

    @FXML
    private void goToEventView() throws IOException {
        navigate("/fxml/event_view.fxml", "Event Management");
    }

    @FXML
    private void goToBlogView() throws IOException {
        navigate("/fxml/AfficherBlogs.fxml", "Blogs");
    }

    @FXML
    private void goToAjouterFormation() throws IOException {
        navigate("/fxml/AjouterFormation.fxml", "Ajouter Formation");
    }

    @FXML
    private void logout() throws IOException {
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
