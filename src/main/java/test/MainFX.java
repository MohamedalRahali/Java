package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Chargement du fichier FXML de test...");
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AjouterBlog.fxml"));
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Ajouter un Blog");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur détaillée : " + e.getMessage());
        }
    }
}