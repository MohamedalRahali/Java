package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import models.Event;
import utils.TranslationService;
import Services.EventService;

import java.io.IOException;
import javafx.application.Platform;

public class ReservationController {
    @FXML private Label eventNameLabel;
    @FXML private Label eventTypeLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label totalPlacesLabel;
    @FXML private Label availablePlacesLabel;
    @FXML private TextField nameField;
    @FXML private TextField nbPlacesField;
    @FXML private TextField emailField;
    @FXML private Button reserveButton;
    @FXML private Button cameraButton;
    @FXML private Button backButton;
    @FXML private Button nameMicButton;
    @FXML private Button placesMicButton;
    @FXML private Button emailMicButton;



    private Event selectedEvent;
    private String currentLanguage = "fr";
    private EventService eventService;
    private TranslationService translationService;

    public void initialize() {

        eventService = new EventService();
        translationService = new TranslationService();
        updateTranslations();
        
        // Désactiver le bouton de réservation par défaut
        reserveButton.setDisable(true);
        
        // Ajouter un listener sur le champ nombre de places
        nbPlacesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbPlacesField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            updateAvailablePlaces();
        });

        // Ajouter le gestionnaire d'événements pour le bouton de réservation
        reserveButton.setOnAction(event -> processReservation());
        
        // Ajouter le gestionnaire d'événements pour le bouton caméra
        cameraButton.setOnAction(event -> openCameraVerification());
        
        // Ajouter le gestionnaire d'événements pour le bouton retour
        backButton.setOnAction(event -> goBack());

        // Initialize speech-to-text buttons
        nameMicButton.setOnAction(event -> showSpeechInputDialog(nameField));
        placesMicButton.setOnAction(event -> showSpeechInputDialog(nbPlacesField));
        emailMicButton.setOnAction(event -> showSpeechInputDialog(emailField));
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
        reserveButton.setText(translationService.translateText("Réserver", currentLanguage));
        backButton.setText(translationService.translateText("Retour", currentLanguage));
    }

    private void openCameraVerification() {
        try {
            // Charger la vue de vérification par caméra
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/camera_verification.fxml"));
            Parent root = loader.load();
            
            CameraVerificationController verificationController = loader.getController();
            
            Stage verificationStage = new Stage();
            verificationStage.setTitle("Vérification par Caméra");
            verificationStage.setScene(new Scene(root));
            verificationStage.initModality(Modality.APPLICATION_MODAL);
            
            // Configurer le gestionnaire de fermeture de la fenêtre de vérification
            verificationStage.setOnHiding(e -> {
                if (verificationController.isPersonVerified()) {
                    reserveButton.setDisable(false);
                    showAlert("Succès", "Vérification réussie! Vous pouvez maintenant réserver.");
                } else {
                    reserveButton.setDisable(true);
                    showAlert("Erreur", "La vérification par caméra a échoué. Veuillez réessayer.");
                }
            });
            
            // Afficher la fenêtre de vérification
            verificationStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture de la caméra");
        }
    }

    private void processReservation() {
        if (nameField.getText().isEmpty() || 
            nbPlacesField.getText().isEmpty() || 
            emailField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        int requestedPlaces;
        try {
            requestedPlaces = Integer.parseInt(nbPlacesField.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le nombre de places doit être un nombre valide");
            return;
        }

        if (requestedPlaces <= 0) {
            showAlert("Erreur", "Le nombre de places doit être supérieur à 0");
            return;
        }

        if (requestedPlaces > selectedEvent.getNb_place_dispo()) {
            showAlert("Erreur", "Il n'y a pas assez de places disponibles");
            return;
        }

        try {
            // Mettre à jour le nombre de places disponibles
            selectedEvent.setNb_place_dispo(selectedEvent.getNb_place_dispo() - requestedPlaces);
            eventService.update(selectedEvent);

            // Charger et afficher le reçu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/receipt_view.fxml"));
            Parent root = loader.load();
            
            ReceiptController receiptController = loader.getController();
            receiptController.setData(selectedEvent, nameField.getText(), emailField.getText(), requestedPlaces);
            receiptController.setLanguage(currentLanguage);
            
            Stage stage = (Stage) reserveButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'affichage du reçu");
        }
    }



    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void showSpeechInputDialog(TextField targetField) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reconnaissance vocale");
        dialog.setHeaderText("Veuillez dicter votre texte");
        dialog.setContentText("Texte dicté:");

        // Customize the dialog
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        dialog.showAndWait().ifPresent(result -> {
            targetField.setText(result);
        });
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_events.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la page");
        }
    }
}
