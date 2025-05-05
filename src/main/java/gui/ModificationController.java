package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reclamation;
import Services.ReclamationService;

import java.io.IOException;

public class ModificationController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;

    private Reclamation reclamationToModify;
    private final ReclamationService service = new ReclamationService();

    public void initData(Reclamation reclamation) {
        this.reclamationToModify = reclamation;

        // Remplir les champs avec les données de la réclamation
        tfTitle.setText(reclamation.getTitle());
        taDescription.setText(reclamation.getDescription());
    }

    @FXML
    private void sauvegarderModification() {
        try {
            // Validate all fields are filled
            if (tfTitle.getText().isEmpty() || taDescription.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs!");
                return;
            }

            // Update reclamation object
            reclamationToModify.setTitle(tfTitle.getText().trim());
            reclamationToModify.setDescription(taDescription.getText().trim());

            // Save to database
            service.modifier(reclamationToModify);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "La réclamation a été modifiée avec succès!");

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/interface_reclamation.fxml"));
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