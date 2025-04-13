package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Event;
import Services.EventService;

import java.io.IOException;
import java.sql.Date;

public class ModificationController {

    @FXML private TextField tfTitle, tfLieux, tfStatus, tfDuree, tfNbPlaces;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpDate;

    private Event eventToModify;
    private final EventService service = new EventService();

    public void initData(Event event) {
        this.eventToModify = event;

        // Remplir les champs avec les données de l'événement
        tfTitle.setText(event.getTitle());
        dpDate.setValue(event.getDate().toLocalDate());
        taDescription.setText(event.getDescription());
        tfLieux.setText(event.getLieux());
        tfStatus.setText(event.getStatus());
        tfDuree.setText(event.getDuree());
        tfNbPlaces.setText(String.valueOf(event.getNb_place_dispo()));
    }

    @FXML
    private void sauvegarderModification() {
        try {
            // Validate all fields are filled
            if (tfTitle.getText().isEmpty() || dpDate.getValue() == null || taDescription.getText().isEmpty() ||
                    tfLieux.getText().isEmpty() || tfStatus.getText().isEmpty() || tfDuree.getText().isEmpty() ||
                    tfNbPlaces.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs!");
                return;
            }

            // Validate number of places is a positive integer
            int nbPlaces;
            try {
                nbPlaces = Integer.parseInt(tfNbPlaces.getText());
                if (nbPlaces <= 0) {
                    showAlert("Erreur", "Le nombre de places doit être un entier positif!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le nombre de places doit être un nombre entier!");
                return;
            }

            // Update event object
            eventToModify.setTitle(tfTitle.getText());
            eventToModify.setDate(Date.valueOf(dpDate.getValue()));
            eventToModify.setDescription(taDescription.getText());
            eventToModify.setLieux(tfLieux.getText());
            eventToModify.setStatus(tfStatus.getText());
            eventToModify.setDuree(tfDuree.getText());
            eventToModify.setNb_place_dispo(nbPlaces);

            // Save to database
            service.modifier(eventToModify);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'événement a été modifié avec succès!");

            // Return to the main screen
            returnToMainScreen();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerModification() {
        returnToMainScreen();
    }

    private void returnToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/interface_event.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tfTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'écran principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}