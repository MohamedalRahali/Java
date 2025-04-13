package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    private void goToEventView(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/event_view.fxml", "Liste des Événements");
    }

    @FXML
    private void goToCreateEvent(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/create_event.fxml", "Gestion des Événements");
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
