package gui;

import Services.TypeEvenementService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.TypeEvenement;

import java.sql.SQLException;

public class CreateTypeController {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;

    private final TypeEvenementService service = new TypeEvenementService();

    @FXML
    private void handleSave() {
        if (nomField.getText().isEmpty()) {
            showAlert("Erreur", "Le nom est obligatoire");
            return;
        }

        try {
            TypeEvenement type = new TypeEvenement(
                    0, // ID auto-généré
                    nomField.getText(),
                    descriptionArea.getText()
            );

            service.ajouter(type);
            ((Stage) nomField.getScene().getWindow()).close();

        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'enregistrement");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}