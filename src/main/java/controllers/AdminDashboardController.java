package esprit.tn.controllers;

import esprit.tn.service.CategorieService;
import esprit.tn.service.ProduitService;
import esprit.tn.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import esprit.tn.entities.Categorie;
import esprit.tn.entities.Produit;
import esprit.tn.entities.User;

import java.io.IOException;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, String> titreColumn;
    @FXML private TableColumn<Produit, Double> prixColumn;
    @FXML private TableColumn<Produit, String> descriptionColumn;
    @FXML private TableView<Categorie> categoriesTable;
    @FXML private TableColumn<Categorie, String> libelleColumn;
    @FXML private TableColumn<Categorie, String> categorieDescriptionColumn;
    @FXML private Button deleteProductButton;
    @FXML private Button deleteCategoryButton;
    @FXML private Button logoutButton;

    private ProduitService produitService;
    private CategorieService categorieService;
    private UserService userService;
    private User currentUser;
    private ObservableList<Produit> produitsList;
    private ObservableList<Categorie> categoriesList;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        currentUser = UserService.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", "Vous n'avez pas les droits d'accès à cette interface.");
            logout();
            return;
        }

        welcomeLabel.setText("Bienvenue, " + currentUser.getUsername() + " (Administrateur)");

        produitService = new ProduitService();
        categorieService = new CategorieService();
        userService = new UserService();

        produitsList = FXCollections.observableArrayList();
        categoriesList = FXCollections.observableArrayList();

        setupTables();
        loadData();
    }

    private void setupTables() {
        // Configuration du tableau des produits
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        produitsTable.setItems(produitsList);

        // Configuration du tableau des catégories
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        categorieDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoriesTable.setItems(categoriesList);
    }

    private void loadData() {
        try {
            produitsList.setAll(produitService.getAllProduits());
            categoriesList.setAll(categorieService.getAllCategories());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        Produit selectedProduit = produitsTable.getSelectionModel().getSelectedItem();

        if (selectedProduit == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un produit à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer ce produit ?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer " + selectedProduit.getTitre());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    produitService.deleteProduit(selectedProduit.getId());
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le produit: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        Categorie selectedCategorie = categoriesTable.getSelectionModel().getSelectedItem();

        if (selectedCategorie == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une catégorie à supprimer.");
            return;
        }

        // Vérifier les produits associés


        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer cette catégorie ?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer " + selectedCategorie.getLibelle());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    System.out.println("Tentative de suppression de la catégorie ID : " + selectedCategorie.getId());
                    boolean success = categorieService.deleteCategorie(selectedCategorie.getId());
                    if (success) {
                        loadData();
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée avec succès.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la catégorie : échec de la requête.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la catégorie : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleViewProduit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/produit/produit.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) produitsTable.getScene().getWindow();
            stage.setTitle("Gestion des Produits");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue Produit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewPanier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/panier/panier.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Votre Panier");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le panier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewClientDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) categoriesTable.getScene().getWindow();
            stage.setTitle("Tableau de Bord Client");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue ClientDashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        logout();
    }

    private void logout() {
        userService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("Connexion");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'interface de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}