package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.scene.control.Button;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.util.Duration;
import utils.SoundPlayer;

import java.io.IOException;

public class HomeController implements Initializable {

    @FXML
    private Button createEventBtn;
    @FXML
    private Button viewEventsBtn;
    @FXML
    private Button statisticsBtn;
    @FXML
    private Button typeEventBtn;
    @FXML
    private Button clientSpaceBtn;
    @FXML
    private FadeTransition fadeIn1;
    @FXML
    private FadeTransition fadeIn2;
    @FXML
    private FadeTransition fadeIn3;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Start animations
        fadeIn1.play();
        fadeIn2.setDelay(Duration.millis(200));
        fadeIn2.play();
        fadeIn3.setDelay(Duration.millis(400));
        fadeIn3.play();
    }

    @FXML
    private void goToCreateEvent(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/create_event.fxml", "Gestion des Événements");
    }

    @FXML
    private void goToEventView(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/event_view.fxml", "Liste des Événements");
    }

    @FXML
    private void goToStatistics(ActionEvent event) throws IOException {
        SoundPlayer.playButtonClickSound();
        loadScene(event, "/fxml/statistics_view.fxml", "Statistiques des Événements");
    }

    @FXML
    private void goToTypeEventManagement(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/type_event.fxml", "Gestion des Types d'Événements");
    }

    @FXML
    private void goToClientEvents(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/client_events.fxml", "Espace Client - Liste des Événements");
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
        ((Stage)((Node) event.getSource()).getScene().getWindow()).close();
    }
}
