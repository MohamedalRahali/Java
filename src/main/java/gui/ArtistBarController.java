package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;

public class ArtistBarController {

    @FXML
    private void goToArtistHome(ActionEvent event) throws IOException {
        loadScene("/fxml/artist_home.fxml", event.getSource());
    }

    @FXML
    private void goToMesEvenements(ActionEvent event) throws IOException {
        loadScene("/fxml/mes_evenements_artist.fxml", event.getSource());
    }

    @FXML
    private void goToCreateEvenement(ActionEvent event) throws IOException {
        loadScene("/fxml/create_evenement_artist.fxml", event.getSource());
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        loadScene("/fxml/login.fxml", event.getSource());
    }

    @FXML
    private void goToArtistProfile(ActionEvent event) throws IOException {
        loadScene("/fxml/artist_profile.fxml", event.getSource());
    }

    // Overloaded for MenuItem support
    @FXML
    private void goToArtistHome() throws IOException {
        loadScene("/fxml/artist_home.fxml", null);
    }
    @FXML
    private void goToMesEvenements() throws IOException {
        loadScene("/fxml/mes_evenements_artist.fxml", null);
    }
    @FXML
    private void goToCreateEvenement() throws IOException {
        loadScene("/fxml/create_evenement_artist.fxml", null);
    }
    @FXML
    private void logout() throws IOException {
        loadScene("/fxml/login.fxml", null);
    }

    @FXML
    private void goToArtistProfile() throws IOException {
        loadScene("/fxml/artist_profile.fxml", null);
    }

    private void loadScene(String fxmlPath, Object eventSource) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = null;
        if (eventSource instanceof Node) {
            stage = (Stage) ((Node) eventSource).getScene().getWindow();
        } else if (eventSource instanceof MenuItem) {
            // Try to get the focused stage
            stage = (Stage) Stage.getWindows().stream().filter(Window::isFocused).findFirst().orElse(null);
        }
        if (stage == null) {
            // fallback: get any showing stage
            stage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        }
        if (stage != null) {
            stage.setScene(new Scene(root));
            stage.show();
        }
    }
}
