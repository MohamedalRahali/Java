package esprit.tn.controllers;

import esprit.tn.service.PanierService;
import esprit.tn.entities.Produit;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.List;

public class PanierController {
    @FXML private VBox cartItemsVBox;
    @FXML private Label totalPriceLabel;
    @FXML private Button payWithStripeButton;

    private PanierService panierService;
    private HostServices hostServices;

    private final String STRIPE_SECRET_KEY = "sk_test_51QxzTC06sEncOfCZUazHjq3672gRylLg5hgp0c2ir3TnLUH6yw2zkgabEaAKrNWH0mC8rStbRxTI9QHRdIHJoTce003rYRFXg5";

    public void setPanierService(PanierService panierService) {
        this.panierService = panierService;
        loadCartItems(); // Recharger les items après initialisation
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
        payWithStripeButton.setDisable(true); // Désactiver par défaut

        if (panierService != null) {
            loadCartItems();
        }
    }

    private void loadCartItems() {
        cartItemsVBox.getChildren().clear();

        if (panierService == null) {
            cartItemsVBox.getChildren().add(new Label("Service panier non disponible"));
            return;
        }

        List<Produit> cartItems = panierService.getCartItems();
        double totalPrice = 0.0;

        if (cartItems.isEmpty()) {
            cartItemsVBox.getChildren().add(new Label("Votre panier est vide."));
            payWithStripeButton.setDisable(true);
        } else {
            for (Produit produit : cartItems) {
                HBox itemBox = createCartItemBox(produit);
                cartItemsVBox.getChildren().add(itemBox);
                totalPrice += produit.getPrix();
            }
            payWithStripeButton.setDisable(false);
        }

        totalPriceLabel.setText(String.format("%.2f €", totalPrice));
    }

    private HBox createCartItemBox(Produit produit) {
        HBox itemBox = new HBox(10);
        itemBox.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 5; " +
                "-fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 5);");

        Label titleLabel = new Label(produit.getTitre());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%.2f €", produit.getPrix()));
        priceLabel.setStyle("-fx-font-size: 14px;");

        Button removeButton = new Button("Supprimer");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        removeButton.setOnAction(event -> handleRemoveItem(produit));

        itemBox.getChildren().addAll(titleLabel, priceLabel, removeButton);
        return itemBox;
    }

    private void handleRemoveItem(Produit produit) {
        panierService.removeProduit(produit);
        loadCartItems();
    }

    @FXML
    private void handlePayWithStripe() {
        if (panierService == null || panierService.getCartItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier Vide", "Votre panier est vide. Ajoutez des produits avant de payer.");
            return;
        }

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://votresite.com/success") // Remplacez par votre URL
                    .setCancelUrl("https://votresite.com/cancel")  // Remplacez par votre URL
                    .addAllLineItem(buildLineItems(panierService.getCartItems()))
                    .build();

            Session session = Session.create(params);
            openInBrowser(session.getUrl());
            panierService.clearCart();
            loadCartItems();

        } catch (StripeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Stripe", "Erreur lors du paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<SessionCreateParams.LineItem> buildLineItems(List<Produit> cartItems) {
        return cartItems.stream()
                .map(produit -> SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("eur")
                                        .setUnitAmount((long)(produit.getPrix() * 100))
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(produit.getTitre())
                                                        .setDescription(produit.getDescription() != null ? produit.getDescription() : "")
                                                        .build())
                                        .build())
                        .build())
                .toList();
    }

    private void openInBrowser(String url) {
        try {
            if (hostServices != null) {
                hostServices.showDocument(url);
            } else {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Navigateur", "Impossible d'ouvrir le navigateur: " + e.getMessage());
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