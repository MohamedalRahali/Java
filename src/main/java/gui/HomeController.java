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
    private void goToBlogView(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/blog_view.fxml", "Liste des Blogs");
    }

    @FXML
    private void goToCreateBlog(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/create_blog.fxml", "Créer un Nouveau Blog");
    }

    @FXML
    private void goToBlogManagement(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/blog_management.fxml", "Gestion des Blogs");
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
}