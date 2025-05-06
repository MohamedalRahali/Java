

package esprit.tn.controllers;

import esprit.tn.service.CategorieService;
import esprit.tn.service.PanierService;
import esprit.tn.service.ProduitService;
import esprit.tn.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import esprit.tn.entities.Categorie;
import esprit.tn.entities.Produit;
import esprit.tn.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ClientDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private ComboBox<Categorie> categorieFilter;
    @FXML private GridPane productsGrid;
    @FXML private Label panierCountLabel;
    @FXML private Button voirPanierButton;
    @FXML private Button logoutButton;

    private ProduitService produitService;
    private CategorieService categorieService;
    private UserService userService;
    private PanierService panierService;
    private User currentUser;

    // Embedded Base64 placeholder image (1x1 gray pixel)
    private static final String BASE64_PLACEHOLDER = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mN8z8DwHwAFAQGCAv6LZwAAAABJRU5ErkJggg==";

    @FXML
    public void initialize() {
        currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté.");
            logout();
            return;
        }

        welcomeLabel.setText("Bienvenue, " + currentUser.getUsername());

        produitService = new ProduitService();
        categorieService = new CategorieService();
        userService = new UserService();
        panierService = new PanierService();

        setupCategorieFilter();
        loadProducts(null);
        updatePanierCount();
    }

    private void setupCategorieFilter() {
        try {
            Categorie allCategories = new Categorie();
            allCategories.setId(0L);
            allCategories.setLibelle("Toutes les catégories");

            List<Categorie> categories = categorieService.getAllCategories();

            categorieFilter.getItems().add(allCategories);
            categorieFilter.getItems().addAll(categories);

            categorieFilter.getSelectionModel().selectFirst();

            categorieFilter.setCellFactory(lv -> new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie categorie, boolean empty) {
                    super.updateItem(categorie, empty);
                    setText(empty || categorie == null ? null : categorie.getLibelle());
                }
            });

            categorieFilter.setButtonCell(new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie categorie, boolean empty) {
                    super.updateItem(categorie, empty);
                    setText(empty || categorie == null ? null : categorie.getLibelle());
                }
            });

            categorieFilter.setOnAction(event -> {
                Categorie selectedCategorie = categorieFilter.getValue();
                if (selectedCategorie != null && selectedCategorie.getId() > 0) {
                    loadProductsByCategory(selectedCategorie.getId());
                } else {
                    loadProducts(null);
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProducts(List<Produit> products) {
        try {
            if (products == null) {
                products = produitService.getAllProduits();
            }

            displayProducts(products);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProductsByCategory(int categoryId) {
        try {
            List<Produit> products = produitService.getByCategory(categoryId);
            displayProducts(products);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayProducts(List<Produit> products) {
        productsGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        final int MAX_COLUMNS = 3;

        for (Produit produit : products) {
            try {
                VBox productCard = createProductCard(produit);

                productsGrid.add(productCard, column, row);

                column++;
                if (column > MAX_COLUMNS - 1) {
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage du produit " + produit.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: white;");

        // Create a container for the image with a fallback background
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-background-color: #e0e0e0; -fx-alignment: center; -fx-min-height: 150; -fx-min-width: 200;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Image image = null;
        String imagePath = produit.getImage();
        String resourceBasePath = "/esprit/tn/images/";
        String placeholderPath = resourceBasePath + "placeholder.jpg";

        // Log the raw image path for debugging
        System.out.println("Product ID: " + produit.getId() + ", Image Path from produit: " + imagePath);

        // Attempt to load the product image
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            // Sanitize the image path (remove any leading slashes or paths, keep only the file name)
            String sanitizedImagePath = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String fullImagePath = resourceBasePath + sanitizedImagePath;
            System.out.println("Attempting to load image: " + fullImagePath);

            try {
                // Check if the resource exists
                URL imageUrl = getClass().getResource(fullImagePath);
                if (imageUrl != null) {
                    image = new Image(imageUrl.toString());
                    System.out.println("Successfully loaded product image: " + fullImagePath);
                } else {
                    System.err.println("Product image not found at: " + fullImagePath);
                }
            } catch (Exception e) {
                System.err.println("Error loading product image for product ID " + produit.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Product image path is null or empty for product ID: " + produit.getId());
        }

        // If product image failed to load, try the placeholder
        if (image == null) {
            System.out.println("Attempting to load placeholder image: " + placeholderPath);
            try {
                URL placeholderUrl = getClass().getResource(placeholderPath);
                if (placeholderUrl != null) {
                    image = new Image(placeholderUrl.toString());
                    System.out.println("Successfully loaded placeholder image: " + placeholderPath);
                } else {
                    System.err.println("Placeholder image not found at: " + placeholderPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading placeholder image: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // If both images failed to load, use the embedded Base64 placeholder
        if (image == null) {
            System.err.println("No image available for product ID " + produit.getId() + ". Using embedded Base64 placeholder.");
            image = new Image(BASE64_PLACEHOLDER);
            Label noImageLabel = new Label("Image Not Found");
            noImageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            imageContainer.getChildren().add(noImageLabel);
        }

        imageView.setImage(image);
        imageContainer.getChildren().add(0, imageView); // Add ImageView at the top

        Label titleLabel = new Label(produit.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label priceLabel = new Label(String.format("%.2f €", produit.getPrix()));
        priceLabel.setStyle("-fx-font-size: 12px;");

        String descriptionText = produit.getDescription();
        if (descriptionText != null && descriptionText.length() > 50) {
            descriptionText = descriptionText.substring(0, 47) + "...";
        }
        Label descriptionLabel = new Label(descriptionText);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px;");

        Button addToCartButton = new Button("Ajouter au panier");
        addToCartButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addToCartButton.setOnAction(event -> handleAddToCart(produit));

        card.getChildren().addAll(imageContainer, titleLabel, priceLabel, descriptionLabel, addToCartButton);

        return card;
    }

    private void handleAddToCart(Produit produit) {
        try {
            panierService.ajouterProduit(produit);
            updatePanierCount();
            //showAlert(Alert.AlertType.INFORMATION, "Succès", produit.getTitre() + " ajouté au panier");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le produit au panier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePanierCount() {
        int count = panierService.getNombreProduits();
        panierCountLabel.setText(String.valueOf(count));
    }

    @FXML
    private void handleVoirPanier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/panier/panier.fxml"));
            Parent root = loader.load();

            PanierController controller = loader.getController();

            if (panierService != null) {
                System.out.println("Transmission du PanierService au contrôleur de panier");
                controller.setPanierService(panierService);
            }

            controller.initialize();

            Stage stage = new Stage();
            stage.setTitle("Votre panier");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le panier: " + e.getMessage());
        }
    }

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

    private void logout() {
        userService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/views/Login.fxml"));
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