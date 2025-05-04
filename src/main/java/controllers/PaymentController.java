package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Event;
import utils.TranslationService;
import Services.EventService;
import java.io.IOException;
import java.util.regex.Pattern;

public class PaymentController {
    @FXML private Label eventNameLabel;
    @FXML private Label eventTypeLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label totalPlacesLabel;
    @FXML private Label availablePlacesLabel;
    @FXML private TextField nbPlacesField;
    @FXML private TextField emailField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private TextField cardholderNameField;
    @FXML private Button payButton;
    @FXML private Button backButton;

    private Event selectedEvent;
    private String currentLanguage = "fr";
    private TranslationService translationService;
    private EventService eventService;

    public void initialize() {
        translationService = new TranslationService();
        eventService = new EventService();
        updateTranslations();
        
        // Ajouter un listener sur le champ nombre de places
        nbPlacesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbPlacesField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            updateAvailablePlaces();
        });

        // Ajouter le gestionnaire d'événements pour le bouton de paiement
        payButton.setOnAction(event -> processPayment());
    }

    public void setEvent(Event event) {
        this.selectedEvent = event;
        updateEventDetails();
    }

    public void setLanguage(String language) {
        this.currentLanguage = language;
        updateTranslations();
    }

    private void updateEventDetails() {
        if (selectedEvent != null) {
            eventNameLabel.setText(selectedEvent.getTitle());
            eventTypeLabel.setText(selectedEvent.getTypeEvent() != null ? selectedEvent.getTypeEvent().getName() : "Non spécifié");
            eventLocationLabel.setText(selectedEvent.getLieux());
            totalPlacesLabel.setText(String.valueOf(selectedEvent.getNb_place_dispo()));
            availablePlacesLabel.setText(String.valueOf(selectedEvent.getNb_place_dispo()));
        }
    }

    private void updateAvailablePlaces() {
        if (selectedEvent != null && !nbPlacesField.getText().isEmpty()) {
            try {
                int requestedPlaces = Integer.parseInt(nbPlacesField.getText());
                int availablePlaces = selectedEvent.getNb_place_dispo();
                
                if (requestedPlaces > availablePlaces) {
                    showAlert(translationService.translateText("Erreur", currentLanguage), 
                             translationService.translateText("Nombre de places non disponible", currentLanguage));
                    nbPlacesField.setText(String.valueOf(availablePlaces));
                }
                
                availablePlacesLabel.setText(String.valueOf(availablePlaces - requestedPlaces));
            } catch (NumberFormatException e) {
                // Ignorer les erreurs de format
            }
        }
    }

    private void updateTranslations() {
        payButton.setText(translationService.translateText("Payer", currentLanguage));
        backButton.setText(translationService.translateText("Retour", currentLanguage));
    }

    private void processPayment() {
        // Vérifier que tous les champs sont remplis
        if (nbPlacesField.getText().isEmpty() || 
            emailField.getText().isEmpty() ||
            cardNumberField.getText().isEmpty() || 
            expiryDateField.getText().isEmpty() || 
            cvvField.getText().isEmpty() || 
            cardholderNameField.getText().isEmpty()) {
            showAlert(translationService.translateText("Erreur", currentLanguage), 
                     translationService.translateText("Veuillez remplir tous les champs", currentLanguage));
            return;
        }

        // Vérifier le format de l'email
        if (!isValidEmail(emailField.getText())) {
            showAlert(translationService.translateText("Erreur", currentLanguage), 
                     translationService.translateText("Format d'email invalide", currentLanguage));
            return;
        }

        // Vérifier que le nombre de places est valide
        try {
            int nbPlaces = Integer.parseInt(nbPlacesField.getText());
            if (nbPlaces <= 0) {
                showAlert(translationService.translateText("Erreur", currentLanguage), 
                         translationService.translateText("Le nombre de places doit être supérieur à 0", currentLanguage));
                return;
            }
            if (nbPlaces > selectedEvent.getNb_place_dispo()) {
                showAlert(translationService.translateText("Erreur", currentLanguage), 
                         translationService.translateText("Nombre de places non disponible", currentLanguage));
                return;
            }

            // Mettre à jour le nombre de places disponibles
            selectedEvent.setNb_place_dispo(selectedEvent.getNb_place_dispo() - nbPlaces);
            eventService.update(selectedEvent);
            
            // Rediriger vers la page de reçu
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/receipt.fxml"));
                Parent root = loader.load();
                
                // Passer les données au contrôleur du reçu
                ReceiptController receiptController = loader.getController();
                receiptController.setData(selectedEvent, cardholderNameField.getText(), emailField.getText(), nbPlaces);
                receiptController.setLanguage(currentLanguage);
                
                Stage stage = (Stage) payButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(translationService.translateText("Erreur", currentLanguage), 
                         translationService.translateText("Impossible de charger la page de reçu", currentLanguage));
            }
        } catch (NumberFormatException e) {
            showAlert(translationService.translateText("Erreur", currentLanguage), 
                     translationService.translateText("Nombre de places invalide", currentLanguage));
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_events.fxml"));
            Parent root = loader.load();
            
            // Passer la langue au contrôleur de la page des événements
            ClientEventsController controller = loader.getController();
            controller.setLanguage(currentLanguage);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 