package esprit.tn.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import java.io.IOException;

public class ArtisteDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private TableView<?> produitsTable;

    @FXML
    private TableColumn<?, ?> titreColumn;

    @FXML
    private TableColumn<?, ?> prixColumn;

    @FXML
    private TableColumn<?, ?> descriptionColumn;

    @FXML
    private TableView<?> categoriesTable;

    @FXML
    private TableColumn<?, ?> libelleColumn;

    @FXML
    private TableColumn<?, ?> categorieDescriptionColumn;


    @FXML
    private void handleLogout() {
        try {
            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();

            // Charger la scène de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login/login.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Login");
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionnellement, afficher une alerte à l'utilisateur
        }
    }

    @FXML
    private void onclickp() {
        try {
            // Check if "Gestion Produit" tab already exists
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText().equals("Gestion Produit")) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            // Load the produit.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/produit/produit.fxml"));
            Parent produitRoot = loader.load();

            // Create a new tab for the produit interface
            Tab produitTab = new Tab("Gestion Produit");
            produitTab.setContent(produitRoot);

            // Add the new tab to the TabPane
            tabPane.getTabs().add(produitTab);

            // Select the newly added tab
            tabPane.getSelectionModel().select(produitTab);
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an alert to the user
        }
    }

    @FXML
    private void onclickc() {
        try {
            // Check if "Gestion Catégorie" tab already exists
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText().equals("Gestion Catégorie")) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            // Load the categorie.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categorie/categorie.fxml"));
            Parent categorieRoot = loader.load();

            // Create a new tab for the categorie interface
            Tab categorieTab = new Tab("Gestion Catégorie");
            categorieTab.setContent(categorieRoot);

            // Add the new tab to the TabPane
            tabPane.getTabs().add(categorieTab);

            // Select the newly added tab
            tabPane.getSelectionModel().select(categorieTab);
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an alert to the user
        }
    }
}